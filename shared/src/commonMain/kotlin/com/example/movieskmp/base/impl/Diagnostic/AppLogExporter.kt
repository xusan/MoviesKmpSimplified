package com.base.impl.Diagnostic

import com.base.abstractions.Diagnostic.IAppLogExporter
import com.base.abstractions.Diagnostic.LogSharingResult
import com.base.abstractions.Essentials.IShare
import com.base.abstractions.Essentials.IDirectoryService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import org.koin.core.component.inject
import kotlin.getValue

internal class AppLogExporter : LoggableService(), IAppLogExporter
{
    private val directoryService: IDirectoryService by inject()
    private val shareFileService: IShare by inject()
    private val KyChat_Logs = "KyChat_Logs"



    override suspend fun ShareLogs(): LogSharingResult
    {
        LogMethodStart(::ShareLogs.name)
        try
        {
            this.removeOldFilesFromCache()

            val date = getUtcDateString()
            val fileName = "${KyChat_Logs}_${date}.zip"
            val filePath = directoryService.GetCacheDir().toPath() / fileName

            // also include censored database into logs folder.
            //this.CopyCensoredDatabaseAsync()

            val compressedLogs = loggingService.GetCompressedLogFileBytes()

            if (compressedLogs == null)
            {
                val result = LogSharingResult(false, Exception("Error: GetCompressedLogFileStream() method returned null."))
                return result
            }

            FileSystem.SYSTEM.sink(filePath).buffer().use { sink ->
                sink.write(compressedLogs)
            }

            shareFileService.RequestShareFile("Sharing compressed logs", filePath.toString())

            val result = LogSharingResult(true, null)
            return result
        }
        catch (exception: Exception)
        {
            loggingService.TrackError(exception)

            val result = LogSharingResult(false, exception)
            return result
        }
    }

    fun removeOldFilesFromCache() {
        LogMethodStart(::removeOldFilesFromCache.name)
        try {
            val cacheDir = directoryService.GetCacheDir().toPath()

            // List all files in the cache directory
            val files = FileSystem.SYSTEM.list(cacheDir)
                .filter { it.name.contains(KyChat_Logs) }

            // Delete each matching file
            for (file in files) {
                FileSystem.SYSTEM.delete(file)
            }
        } catch (e: Exception) {
            loggingService.TrackError(e)
        }
    }

    fun getUtcDateString(): String {
        LogMethodStart(::getUtcDateString.name)
        val now = Clock.System.now()
        val utcDateTime = now.toLocalDateTime(TimeZone.UTC)
        return buildString {
            append(utcDateTime.year.toString().padStart(4, '0'))
            append(utcDateTime.monthNumber.toString().padStart(2, '0'))
            append(utcDateTime.dayOfMonth.toString().padStart(2, '0'))
            append(utcDateTime.hour.toString().padStart(2, '0'))
            append(utcDateTime.minute.toString().padStart(2, '0'))
            append(utcDateTime.second.toString().padStart(2, '0'))
        }
    }
}