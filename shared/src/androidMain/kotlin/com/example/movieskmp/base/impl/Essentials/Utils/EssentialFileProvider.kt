package com.base.impl.Droid.Essentials.Utils


import android.os.Environment
import androidx.core.content.FileProvider
import android.net.Uri as AndroidUri
import java.io.File
import android.os.Build
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity

internal class EssentialFileProvider : FileProvider()
{
    companion object
    {
        lateinit var specificLogger: ILogging
        var isLoggerInitialized = false

        internal var AlwaysFailExternalMediaAccess: Boolean = false

        // This allows us to override the default temporary file location of Preferring external but falling back to internal
        // We can choose external only, or internal only as alternative options
        var TemporaryLocation: FileProviderLocation = FileProviderLocation.PreferExternal

        internal val Authority: String
            get() = CurrentActivity.Instance.packageName + ".fileProvider"

        internal fun GetTemporaryRootDirectory(): File
        {
            InitLogger()

            specificLogger.LogMethodStarted("EssentialFileProvider", "GetTemporaryRootDirectory")
            // If we specifically want the internal storage, no extra checks are needed, we have permission
            if (TemporaryLocation == FileProviderLocation.Internal)
                return CurrentActivity.Instance.cacheDir

            // If we explicitly want only external locations we need to do some permissions checking
            val externalOnly = TemporaryLocation == FileProviderLocation.External

            // make sure the external storage is available
            var hasExternalMedia = CurrentActivity.Instance.externalCacheDir != null && IsMediaMounted(CurrentActivity.Instance.externalCacheDir!!)

            // undo all the work if we have requested a fail (mainly for testing)
            if (AlwaysFailExternalMediaAccess)
                hasExternalMedia = false

            // fail if we need the external storage, but there is none
            if (externalOnly && !hasExternalMedia)
                throw InvalidOperationException("Unable to access the external storage, the media is not mounted.")

            // based on permssions, return the correct directory
            // if permission were required, then it would have already thrown
            return if (hasExternalMedia)
                CurrentActivity.Instance.externalCacheDir!!
            else
                CurrentActivity.Instance.cacheDir
        }

        private fun IsMediaMounted(location: File): Boolean
        {
            InitLogger()

            specificLogger.LogMethodStarted("EssentialFileProvider", "IsMediaMounted")
            return Environment.getExternalStorageState(location) == Environment.MEDIA_MOUNTED
        }

        internal fun IsFileInPublicLocation(filename: String): Boolean
        {
            InitLogger()

            specificLogger.LogMethodStarted("EssentialFileProvider", "IsFileInPublicLocation")
            // get the Android path, we use "CanonicalPath" instead of "AbsolutePath"
            // because we want to resolve any ".." and links/redirects
            val file = File(filename)
            val canonicalFilename = file.canonicalPath

            // the shared paths from the "microsoft_maui_essentials_fileprovider_file_paths.xml" resource
            val publicLocations = mutableListOf<String?>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                publicLocations.add(CurrentActivity.Instance.getExternalFilesDir(null)?.canonicalPath)
            }
            else
            {
                publicLocations.add(Environment.getExternalStorageDirectory()?.canonicalPath)
            }

            publicLocations.add(CurrentActivity.Instance.externalCacheDir?.canonicalPath)

            // the internal cache path is available only by file provider in N+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                publicLocations.add(CurrentActivity.Instance?.cacheDir?.canonicalPath)

            for (location in publicLocations)
            {
                if (location.isNullOrWhiteSpace())
                    continue

                // make sure we have a trailing slash
                val suffixedPath = if (canonicalFilename.endsWith(File.separator))
                    canonicalFilename
                else
                    canonicalFilename + File.separator

                if(location!=null) {
                    // check if the requested file is in a folder
                    if (suffixedPath.startsWith(location, ignoreCase = true))
                        return true
                }
            }

            return false
        }

        internal fun GetUriForFile(file: File): AndroidUri
        {
            InitLogger()
            specificLogger.LogMethodStarted("EssentialFileProvider", "GetUriForFile")
            return androidx.core.content.FileProvider.getUriForFile(CurrentActivity.Instance, Authority, file)
        }

        fun InitLogger()
        {
            if(isLoggerInitialized == false)
            {
                val loggingService = ContainerLocator.Resolve<ILoggingService>()
                specificLogger = loggingService.CreateSpecificLogger(SpecificLoggingKeys.LogEssentialServices)
                isLoggerInitialized = true
            }
        }
    }
}

enum class FileProviderLocation
{
    PreferExternal,
    Internal,
    External
}

class InvalidOperationException(message: String) : Exception(message)

fun String?.isNullOrWhiteSpace(): Boolean
{
    return this == null || this.trim().isEmpty()
}


