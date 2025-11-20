package com.base.impl.Droid.Diagnostic

import com.base.abstractions.Diagnostic.IFileLogger
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.FileAppender
import com.base.abstractions.Essentials.IDirectoryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


internal class DroidLogbackFileLogger : KoinComponent, IFileLogger
{
    val directoryService: IDirectoryService by inject()
    private lateinit var logger: Logger
    private lateinit var logDir: String
    private lateinit var logFileName: String
    private lateinit var currentLogPath: String
    override fun Init()
    {
        logDir = GetLogsFolder()

        val dateFormatDay = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayStamp = dateFormatDay.format(Date())
        val sessionFolder = File(logDir, dayStamp)
        if (!sessionFolder.exists())
            sessionFolder.mkdirs()

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        logFileName = "session_$timestamp.log"
        currentLogPath = File(sessionFolder, logFileName).absolutePath

        // Configure Logback programmatically
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()

        val encoder = PatternLayoutEncoder().apply {
            this.context = context
            this.pattern = "%msg%n" // message only
            this.charset = StandardCharsets.UTF_8
            start()
        }

        val fileAppender = FileAppender<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
            this.context = context
            this.file = currentLogPath
            this.encoder = encoder
            this.isAppend = true
            start()
        }

        val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.DEBUG
        rootLogger.addAppender(fileAppender)

        logger = LoggerFactory.getLogger(DroidLogbackFileLogger::class.java) as Logger

        cleanupOldLogs()
    }

    override fun Info(message: String)
    {
        logger.info(message)
    }

    override fun Warn(message: String)
    {
        logger.warn(message)
    }

    override fun Error(message: String)
    {
        logger.error(message)
    }

    override suspend fun GetCompressedLogsSync(getOnlyLastSession: Boolean): ByteArray?
    {
        // logback doesn’t have flush/pause like NLog — just flush file streams if needed
        return if (getOnlyLastSession)
        {
            zipLastSessionToStream()
        }
        else
        {
            zipMainFolderToStream(logDir)
        }
    }

    override suspend fun GetLogListAsync(): List<String> = withContext(Dispatchers.IO) {
        val file = File(currentLogPath)
        if (!file.exists()) return@withContext emptyList<String>()

        val lines = file.readLines()
        val lineCount = 100
        if (lines.size <= lineCount) return@withContext lines
        else return@withContext lines.takeLast(lineCount)
    }

    override fun GetLogsFolder(): String
    {
        val appDataDir = directoryService.GetAppDataDir()
        val path = File(appDataDir, "Logback")
        if (!path.exists())
            path.mkdirs()

        return path.absolutePath
    }

    override fun GetCurrentLogFileName(): String
    {
        return currentLogPath
    }

    /**
     * Keep only last 7 days folders.
     */
    private fun cleanupOldLogs()
    {
        try
        {
            val logRoot = File(logDir)
            if (!logRoot.exists()) return

            val dayFolders = logRoot.listFiles { file ->
                file.isDirectory && Regex("\\d{4}-\\d{2}-\\d{2}").matches(file.name)
            }?.sortedByDescending { it.name } ?: return

            if (dayFolders.size > 7)
            {
                val oldFolders = dayFolders.drop(7)
                for (folder in oldFolders)
                {
                    try
                    {
                        folder.deleteRecursively()
                    }
                    catch (ex: Exception)
                    {
                        println("Failed to delete ${folder.absolutePath}: $ex")
                    }
                }
            }
            else
            {
                println("Skip cleanup: less than 7 log folders.")
            }
        }
        catch (ex: Exception)
        {
            println("Cleanup error: $ex")
        }
    }

    private fun zipMainFolderToStream(folderPath: String): ByteArray
    {
        val folder = File(folderPath)
        if (!folder.exists()) throw FileNotFoundException("Folder not found: $folderPath")

        val memoryStream = ByteArrayOutputStream()
        ZipOutputStream(memoryStream).use { zip ->
            folder.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(folder).path
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
        return memoryStream.toByteArray()
    }

    private fun zipLastSessionToStream(): ByteArray
    {
        val file = File(currentLogPath)
        if (!file.exists())
            throw FileNotFoundException("File not found: $currentLogPath")

        val memoryStream = ByteArrayOutputStream()
        ZipOutputStream(memoryStream).use { zip ->
            zip.putNextEntry(ZipEntry(file.name))
            file.inputStream().use { input -> input.copyTo(zip) }
            zip.closeEntry()
        }
        return memoryStream.toByteArray()
    }


}