package com.base.impl.Droid.Essentials.Utils

import android.app.Application
import android.net.Uri as AndroidUri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.BaseColumns as IBaseColumns
import android.webkit.MimeTypeMap
import com.base.impl.Droid.Utils.CurrentActivity
import java.io.File as JavaFile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.pathString

internal object FileSystemUtils
{
    internal const val EssentialsFolderHash = "2203693cc04e0be7f4f024d5f9499e13"

    private const val storageTypePrimary = "primary"
    private const val storageTypeRaw = "raw"
    private const val storageTypeImage = "image"
    private const val storageTypeVideo = "video"
    private const val storageTypeAudio = "audio"

    private val contentUriPrefixes = arrayOf(
        "content://downloads/public_downloads",
        "content://downloads/my_downloads",
        "content://downloads/all_downloads"
    )

    internal const val UriSchemeFile = "file"
    internal const val UriSchemeContent = "content"

    internal const val UriAuthorityExternalStorage = "com.android.externalstorage.documents"
    internal const val UriAuthorityDownloads = "com.android.providers.downloads.documents"
    internal const val UriAuthorityMedia = "com.android.providers.media.documents"

    fun GetTemporaryFile(root: JavaFile, fileName: String): JavaFile
    {
        // create the directory for all Essentials files
        val rootDir = JavaFile(root, EssentialsFolderHash)
        rootDir.mkdirs()
        rootDir.deleteOnExit()

        // create a unique directory just in case there are multiple file with the same name
        val tmpDir = JavaFile(rootDir, UUID.randomUUID().toString().replace("-", ""))
        tmpDir.mkdirs()
        tmpDir.deleteOnExit()

        // create the new temporary file
        val tmpFile = JavaFile(tmpDir, fileName)
        tmpFile.deleteOnExit()

        return tmpFile
    }

//    fun EnsurePhysicalPath(uri: AndroidUri, requireExtendedAccess: Boolean = true): String
//    {
//        // if this is a file, use that
//        if (uri.scheme.equals(UriSchemeFile, ignoreCase = true))
//            return uri.path!!
//
//        // try resolve using the content provider
//        val absolute = ResolvePhysicalPath(uri, requireExtendedAccess)
//        if (!absolute.isNullOrWhiteSpace() && java.io.File(absolute).isAbsolute)
//            return absolute ?: ""
//
//        // fall back to just copying it
//        val cached = CacheContentFile(uri)
//        if (!cached.isNullOrWhiteSpace() && java.io.File(cached).isAbsolute)
//            return cached ?: ""
//
//        throw java.io.FileNotFoundException("Unable to resolve absolute path or retrieve contents of URI '$uri'.")
//    }

    private fun ResolvePhysicalPath(uri: AndroidUri, requireExtendedAccess: Boolean = true): String?
    {
        if (uri.scheme.equals(UriSchemeFile, ignoreCase = true))
        {
            // if it is a file, then return directly

            val resolved = uri.path!!
            if (java.io.File(resolved).exists())
                return resolved
        }
        else if (!requireExtendedAccess || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q)
        {
            // if this is on an older OS version, or we just need it now

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT &&
                DocumentsContract.isDocumentUri(CurrentActivity.Instance, uri))
            {
                val resolved = ResolveDocumentPath(uri)
                if (resolved != null && java.io.File(resolved).exists())
                    return resolved
            }
            else if (uri.scheme.equals(UriSchemeContent, ignoreCase = true))
            {
                val resolved = ResolveContentPath(uri)
                if (resolved != null && java.io.File(resolved).exists())
                    return resolved
            }
        }

        return null
    }

    private fun ResolveDocumentPath(uri: AndroidUri): String?
    {
        android.util.Log.d("FileSystemUtils", "Trying to resolve document URI: '$uri'")

        val docId = DocumentsContract.getDocumentId(uri)

        val docIdParts = docId?.split(':')
        if (docIdParts == null || docIdParts.isEmpty())
            return null

        if (uri.authority.equals(UriAuthorityExternalStorage, ignoreCase = true))
        {
            android.util.Log.d("FileSystemUtils", "Resolving external storage URI: '$uri'")

            if (docIdParts.size == 2)
            {
                val storageType = docIdParts[0]
                val uriPath = docIdParts[1]

                // This is the internal "external" memory, NOT the SD Card
                if (storageType.equals(storageTypePrimary, ignoreCase = true))
                {
                    val root = Environment.getExternalStorageDirectory().path

                    return java.io.File(root, uriPath).path
                }

                // TODO: support other types, such as actual SD Cards
            }
        }
        else if (uri.authority.equals(UriAuthorityDownloads, ignoreCase = true))
        {
            android.util.Log.d("FileSystemUtils", "Resolving downloads URI: '$uri'")

            // NOTE: This only really applies to older Android vesions since the privacy changes

            if (docIdParts.size == 2)
            {
                val storageType = docIdParts[0]
                val uriPath = docIdParts[1]

                if (storageType.equals(storageTypeRaw, ignoreCase = true))
                    return uriPath
            }

            // ID could be "###" or "msf:###"
            val fileId = if (docIdParts.size == 2)
                docIdParts[1]
            else
                docIdParts[0]

            for (prefix in contentUriPrefixes)
            {
                val uriString = "$prefix/$fileId"
                val contentUri = AndroidUri.parse(uriString)

                val filePath = GetDataFilePath(contentUri)
                if (filePath != null)
                    return filePath
            }
        }
        else if (uri.authority.equals(UriAuthorityMedia, ignoreCase = true))
        {
            android.util.Log.d("FileSystemUtils", "Resolving media URI: '$uri'")

            if (docIdParts.size == 2)
            {
                val storageType = docIdParts[0]
                val uriPath = docIdParts[1]

                var contentUri: AndroidUri? = null
                if (storageType.equals(storageTypeImage, ignoreCase = true))
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else if (storageType.equals(storageTypeVideo, ignoreCase = true))
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else if (storageType.equals(storageTypeAudio, ignoreCase = true))
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                if (contentUri != null)
                {
                    val filePath = GetDataFilePath(contentUri, "${IBaseColumns._ID}=?", arrayOf(uriPath))
                    if (filePath != null)
                        return filePath
                }
            }
        }

        android.util.Log.d("FileSystemUtils", "Unable to resolve document URI: '$uri'")

        return null
    }

    private fun ResolveContentPath(uri: AndroidUri): String?
    {
        android.util.Log.d("FileSystemUtils", "Trying to resolve content URI: '$uri'")

        val filePath = GetDataFilePath(uri)
        if (filePath != null)
            return filePath

        // TODO: support some additional things, like Google Photos if that is possible

        android.util.Log.d("FileSystemUtils", "Unable to resolve content URI: '$uri'")

        return null
    }

//    private fun CacheContentFile(uri: AndroidUri): String?
//    {
//        if (!uri.scheme.equals(UriSchemeContent, ignoreCase = true))
//            return null
//
//        android.util.Log.d("FileSystemUtils", "Copying content URI to local cache: '$uri'")
//
//        // open the source stream
//        val streamResult = OpenContentStream(uri)
//        val srcStream = streamResult.first
//        val extension = streamResult.second
//
//        if (srcStream == null)
//            return null
//
//        srcStream.use { input ->
//            // resolve or generate a valid destination path
//            var filename = GetColumnValue(uri, MediaStore.MediaColumns.DISPLAY_NAME)
//                ?: UUID.randomUUID().toString().replace("-", "")
//
//            if (!hasExtension(filename) && !extension.isNullOrEmpty())
//                filename = changeExtension(filename, extension)
//
//            // create a temporary file
//            val hasPermission = Permissions.IsDeclaredInManifest(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            val root = if (hasPermission)
//                CurrentActivity.Instance.externalCacheDir
//            else
//                CurrentActivity.Instance.cacheDir
//            val tmpFile = GetTemporaryFile(root!!, filename)
//
//            // copy to the destination
//            FileOutputStream(tmpFile.canonicalPath).use { output ->
//                input.copyTo(output)
//            }
//
//            return tmpFile.canonicalPath
//        }
//    }

    private fun OpenContentStream(uri: AndroidUri): Pair<InputStream?, String?>
    {
        val isVirtual = IsVirtualFile(uri)
        if (isVirtual)
        {
            android.util.Log.d("FileSystemUtils", "Content URI was virtual: '$uri'")
            return GetVirtualFileStream(uri)
        }

        val extension = GetFileExtension(uri)
        val stream = CurrentActivity.Instance.contentResolver.openInputStream(uri)
        return Pair(stream, extension)
    }

    private fun IsVirtualFile(uri: AndroidUri): Boolean
    {
        if (!DocumentsContract.isDocumentUri(CurrentActivity.Instance, uri))
            return false

        val value = GetColumnValue(uri, DocumentsContract.Document.COLUMN_FLAGS)
        if (!value.isNullOrEmpty())
        {
            val flagsInt = value.toIntOrNull()
            if (flagsInt != null)
            {
                val flags = flagsInt

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    return (flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0
            }
        }

        return false
    }

    private fun GetVirtualFileStream(uri: AndroidUri): Pair<InputStream?, String?>
    {
        val mimeTypes = CurrentActivity.Instance.contentResolver.getStreamTypes(uri, "*/*")
        if (mimeTypes != null && mimeTypes.isNotEmpty())
        {
            val mimeType = mimeTypes[0]

            val stream = CurrentActivity.Instance.contentResolver
                .openTypedAssetFileDescriptor(uri, mimeType, null)
                ?.createInputStream()

            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

            return Pair(stream, extension)
        }

        return Pair(null, null)
    }

    private fun GetColumnValue(
        contentUri: AndroidUri,
        column: String,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): String?
    {
        try
        {
            val value = QueryContentResolverColumn(contentUri, column, selection, selectionArgs)
            if (!value.isNullOrEmpty())
                return value
        }
        catch (e: Exception)
        {
            // Ignore all exceptions and use null for the error indicator
        }

        return null
    }

    private fun GetDataFilePath(
        contentUri: AndroidUri,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): String?
    {
        val column = MediaStore.MediaColumns.DATA

        // ask the content provider for the data column, which may contain the actual file path
        val path = GetColumnValue(contentUri, column, selection, selectionArgs)
        if (!path.isNullOrEmpty() && java.io.File(path).isAbsolute)
            return path

        return null
    }

    private fun GetFileExtension(uri: AndroidUri): String?
    {
        val mimeType = CurrentActivity.Instance.contentResolver.getType(uri)

        return if (mimeType != null)
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        else
            null
    }

    private fun QueryContentResolverColumn(
        contentUri: AndroidUri,
        columnName: String,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): String?
    {
        var text: String? = null

        val projection = arrayOf(columnName)
        val cursor = CurrentActivity.Instance.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst())
            {
                val columnIndex = it.getColumnIndex(columnName)
                if (columnIndex != -1)
                    text = it.getString(columnIndex)
            }
        }

        return text
    }

    internal fun GetShareableFileUri(fullPath: String): AndroidUri
    {
        var sharedFile = JavaFile(fullPath)
        if (!EssentialFileProvider.IsFileInPublicLocation(fullPath))
        {
            val root = EssentialFileProvider.GetTemporaryRootDirectory()

            val tmpFile = GetTemporaryFile(root, sharedFile.name)

            java.io.File(fullPath).copyTo(java.io.File(tmpFile.canonicalPath), overwrite = true)

            sharedFile = tmpFile
        }

        // create the uri, if N use file provider
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
        {
            return EssentialFileProvider.GetUriForFile(sharedFile)
        }

        // use the shared file path created
        return AndroidUri.fromFile(sharedFile)
    }

    private fun String?.isNullOrWhiteSpace(): Boolean
    {
        return this == null || this.trim().isEmpty()
    }

    private fun hasExtension(filename: String): Boolean
    {
        return filename.contains('.') && filename.lastIndexOf('.') < filename.length - 1
    }

    private fun changeExtension(filename: String, extension: String): String
    {
        val lastDot = filename.lastIndexOf('.')
        val nameWithoutExtension = if (lastDot >= 0) filename.substring(0, lastDot) else filename
        val ext = if (extension.startsWith('.')) extension else ".$extension"
        return nameWithoutExtension + ext
    }
}


