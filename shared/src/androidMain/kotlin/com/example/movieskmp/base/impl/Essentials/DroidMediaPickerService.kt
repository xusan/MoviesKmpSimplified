package com.base.impl.Droid.Essentials

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Essentials.IMediaPickerService
import com.base.abstractions.Essentials.MediaFile
import com.base.abstractions.Essentials.MediaOptions
import com.base.abstractions.Essentials.MediaSource
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Essentials.Utils.InvalidOperationException
import com.base.impl.Droid.Utils.CurrentActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DroidMediaPickerService : LoggableService(), IMediaPickerService
{
    private var activity: ComponentActivity
    private var fileProviderAuthority: String
    private var pending: CompletableDeferred<Uri?>? = null
    private var cameraOutputUri: Uri? = null

    private var getContentLauncher: ActivityResultLauncher<String>? = null
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null

    init
    {
        activity = CurrentActivity.Instance
        fileProviderAuthority = "${activity.packageName}.media.fileprovider"
        InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)

        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
            throw IllegalStateException("Call IMediaPickerService.Initialize() before MainActivity.onCreate() finishes")

        getContentLauncher = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            pending?.complete(uri)
            pending = null
        }

        takePictureLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val resultUri = if (success) cameraOutputUri else null
            pending?.complete(resultUri)
            pending = null
        }
    }

    override suspend fun GetPhotoAsync(options: MediaOptions): MediaFile?
    {
        SpecificLogMethodStart(::GetPhotoAsync.name, options)
        return pickImage(MediaSource.GALLERY, options)
    }

    override suspend fun TakePhotoAsync(options: MediaOptions): MediaFile?
    {
        SpecificLogMethodStart(::TakePhotoAsync.name, options)
        return pickImage(MediaSource.CAMERA, options)
    }

    private suspend fun pickImage(source: MediaSource, options: MediaOptions): MediaFile?
    {
        SpecificLogMethodStart(::pickImage.name, source, options)
        // ensure only one in-flight request
        if (pending != null) return null

        val uri = when (source)
        {
            MediaSource.GALLERY -> awaitFromGallery()
            MediaSource.CAMERA -> awaitFromCamera()
        } ?: return null

        // Resolve to absolute file path (copying if needed or if saveToAppDirectory = true)
        val (absFile, mime) = withContext(Dispatchers.IO) {
            val result = resolveToAbsoluteFile(src = uri, saveToAppDir = options.saveToAppDirectory)
            result
        }

        // Optional resize/compress (ScaleToFit) → write back into the same absFile
        if ((options.maxWidth != null || options.maxHeight != null) || options.compress)
        {
            withContext(Dispatchers.IO) {
                resizeAndOrCompressToFile(
                    inputFile = absFile,
                    outputFile = absFile, // overwrite
                    maxW = options.maxWidth,
                    maxH = options.maxHeight,
                    compress = options.compress,
                    quality = options.compressionQuality
                )
            }
        }

        val bytes: ByteArray? = if (options.includeBytes)
        {
            withContext(Dispatchers.IO) { absFile.readBytes() }
        }
        else null

        return MediaFile(
            FilePath = absFile.absolutePath,
            MimeType = mime ?: guessMimeFromPath(absFile.absolutePath),
            ByteData = bytes
        )
    }

    // ————— Launchers —————

    private suspend fun awaitFromGallery(): Uri? = suspendCancellableCoroutine { cont ->

        SpecificLogMethodStart(::awaitFromGallery.name)

        if (getContentLauncher == null)
            throw InvalidOperationException("Please call IMediaPickerService.Initialize() first.")

        val def = CompletableDeferred<Uri?>()
        pending = def
        try {
            getContentLauncher!!.launch("image/*")
        } catch (t: Throwable) {
            pending = null
            cont.resumeWithException(t)
            return@suspendCancellableCoroutine
        }

        cont.invokeOnCancellation {
            pending?.cancel()
            pending = null
        }

        def.invokeOnCompletion {
            val res = runCatching { def.getCompleted() }.getOrNull()
            if (cont.isActive) cont.resume(res)
        }
    }

    private suspend fun awaitFromCamera(): Uri? = suspendCancellableCoroutine { cont ->

        SpecificLogMethodStart(::awaitFromCamera.name)

        if (takePictureLauncher == null)
            throw InvalidOperationException("Please call IMediaPickerService.Initialize() first.")

        val def = CompletableDeferred<Uri?>()
        pending = def
        val (uri, error) = runCatching { createCameraOutputUri() }.fold(
            onSuccess = { it to null },
            onFailure = { null to it })
        if (error != null) {
            pending = null
            cont.resumeWithException(error)
            return@suspendCancellableCoroutine
        }
        cameraOutputUri = uri

        try {
            takePictureLauncher!!.launch(uri)
        } catch (t: Throwable) {
            pending = null
            cont.resumeWithException(t)
            return@suspendCancellableCoroutine
        }

        cont.invokeOnCancellation {
            pending?.cancel()
            pending = null
        }

        def.invokeOnCompletion {
            val res = runCatching { def.getCompleted() }.getOrNull()
            if (cont.isActive) cont.resume(res)
        }
    }

    // ————— File & image utils —————

    private fun createCameraOutputUri(): Uri
    {
        SpecificLogMethodStart(::createCameraOutputUri.name)
        val picturesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: activity.filesDir
        val file = File(
            picturesDir,
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
        )
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        return FileProvider.getUriForFile(activity, fileProviderAuthority, file)
    }

    private fun resolveToAbsoluteFile(src: Uri, saveToAppDir: Boolean): Pair<File, String?>
    {
        val scheme = src.scheme
        val cr = activity.contentResolver

        val mime = try
        {
            cr.getType(src)
        }
        catch (_: Throwable)
        {
            null
        }

        // If caller wants us to always save to app dir, or we can't get a direct path, copy.
        if (saveToAppDir || scheme == ContentResolver.SCHEME_CONTENT || scheme == ContentResolver.SCHEME_ANDROID_RESOURCE)
        {
            val targetDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: activity.filesDir
            val ext = when
            {
                mime?.contains("png", true) == true -> ".png"
                mime?.contains("webp", true) == true -> ".webp"
                else -> ".jpg"
            }
            val outFile = File(
                targetDir,
                "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}$ext"
            )
            copyUriToFile(cr, src, outFile)
            return outFile to (mime ?: guessMimeFromPath(outFile.absolutePath))
        }
        else
        {
            // Try to treat as file://
            val path = src.path
            if (!path.isNullOrEmpty())
            {
                val f = File(path)
                if (f.exists()) return f to (mime ?: guessMimeFromPath(path))
            }
            // Fallback: copy
            val targetDir = activity.cacheDir
            val outFile = File(targetDir, "IMG_${System.currentTimeMillis()}.jpg")
            copyUriToFile(cr, src, outFile)
            return outFile to (mime ?: guessMimeFromPath(outFile.absolutePath))
        }
    }

    private fun copyUriToFile(cr: ContentResolver, uri: Uri, outFile: File)
    {
        SpecificLogMethodStart(::copyUriToFile.name)

        cr.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open input stream for $uri")
    }

    /**
     * ScaleToFit (maintain aspect ratio) + optional JPEG/WebP compression.
     * If no resize & compress=false → just return (keeps file as is).
     */
    private fun resizeAndOrCompressToFile(inputFile: File, outputFile: File, maxW: Int?, maxH: Int?, compress: Boolean, quality: Int)
    {
        SpecificLogMethodStart(::resizeAndOrCompressToFile.name)

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(inputFile.absolutePath, options)

        val (srcW, srcH) = options.outWidth to options.outHeight
        if (srcW <= 0 || srcH <= 0) return

        val (targetW, targetH) = computeScaleToFit(srcW, srcH, maxW, maxH)

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(srcW, srcH, targetW, targetH)
        }

        val decoded = BitmapFactory.decodeFile(inputFile.absolutePath, decodeOpts) ?: return
        val scaled = if (decoded.width != targetW || decoded.height != targetH)
        {
            Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                if (it != decoded) decoded.recycle()
            }
        }
        else decoded

        FileOutputStream(outputFile).use { fos ->
            // Prefer JPEG for general photos; if original looks like PNG, you could detect and switch.
            val format = Bitmap.CompressFormat.JPEG
            val q = if (compress) quality.coerceIn(0, 100) else 100
            scaled.compress(format, q, fos)
        }
        if (!scaled.isRecycled) scaled.recycle()
    }

    private fun computeScaleToFit(srcW: Int, srcH: Int, maxW: Int?, maxH: Int?): Pair<Int, Int>
    {
        SpecificLogMethodStart(::computeScaleToFit.name)

        if (maxW == null && maxH == null) return srcW to srcH
        val mw = maxW ?: Int.MAX_VALUE
        val mh = maxH ?: Int.MAX_VALUE
        val ratio = minOf(mw.toFloat() / srcW, mh.toFloat() / srcH)
        return if (ratio >= 1f) srcW to srcH
        else (srcW * ratio).toInt().coerceAtLeast(1) to (srcH * ratio).toInt().coerceAtLeast(1)
    }

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int
    {
        SpecificLogMethodStart(::calculateInSampleSize.name)

        var inSampleSize = 1
        if (srcH > reqH || srcW > reqW)
        {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW)
            {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun guessMimeFromPath(path: String): String?
    {
        SpecificLogMethodStart(::guessMimeFromPath.name)

       return when {
            path.endsWith(".png", true) -> "image/png"
            path.endsWith(".webp", true) -> "image/webp"
            path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) -> "image/jpeg"
            else -> "image/*"
        }
    }
}