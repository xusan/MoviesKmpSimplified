package com.base.impl.Diagnostic

import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.IPlatformOutput
import com.base.abstractions.Essentials.IPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class AppLoggingService : KoinComponent, ILoggingService
{
    private var RowNumber = 0L
    private val ENTER_TAG = "➡Enter"
    private val EXIT_TAG = "🏃Exit"
    private val INDICATOR_TAG = "⏱Indicator_"
    private val HANDLED_ERROR = "💥Handled Exception: "
    private val UNHANDLED_ERROR = "💥Crash Unhandled: "

    private var AppLaunchCount = 0;
    override var LastError: Throwable? = null
    override val HasError: Boolean get() = LastError != null

    private val errorTrackingService: IErrorTrackingService by inject()
    private val fileLogger: IFileLogger by inject()
    private val preferences: IPreferences by inject()
    private val platformConsole: IPlatformOutput by inject()

    init
    {
        errorTrackingService.OnServiceError += ::ErrorTrackingService_OnError

        fileLogger.Init()
        AppLaunchCount = GetLaunchCount()
    }

    private fun ErrorTrackingService_OnError(ex: Throwable)
    {
        LogError(ex, "Error happens in IErrorTrackingService", handled = true);
    }

    override fun TrackError(ex: Throwable, data: Map<String, String>?)
    {
        TrackInternal(ex, handled = true, data)
    }

    override fun LogUnhandledError(ex: Throwable)
    {
        TrackInternal(ex, handled = false)
    }

    private fun TrackInternal(ex: Throwable, handled: Boolean, data: Map<String, String>? = null)
    {
        SafeCall()
        {
            LastError = ex
            LogError(ex, "", handled)

            if (handled)
            {
                GlobalScope.launch()
                {
                    try
                    {
                        errorTrackingService.TrackError(ex, null)
                    }
                    catch (ex: Throwable)
                    {
                        LogError(ex, "Failed to track error")
                    }
                }
            }
        }
    }

    override fun Log(message: String)
    {
        SafeCall()
        {
            RowNumber++
            val tag = GetLogAppTag(AppLaunchCount, RowNumber)
            val formatted = "$tag INFO:$message"
            fileLogger.Info(formatted)
            platformConsole.Info(formatted)
        }
    }

    override fun LogWarning(message: String)
    {
        SafeCall()
        {
            RowNumber++
            val tag = GetLogAppTag(AppLaunchCount, RowNumber)
            val formatted = "$tag WARNING:$message"
            fileLogger.Warn(formatted)
            platformConsole.Warn(formatted)
        }
    }

    override fun LogError(ex: Throwable, message: String, handled: Boolean)
    {
        SafeCall()
        {
            RowNumber++
            val tag = GetLogAppTag(AppLaunchCount, RowNumber)

            val formatted = buildString()
            {
                append("$tag ERROR: ")
                if (handled)
                    append(HANDLED_ERROR)
                else
                    append(UNHANDLED_ERROR)

                if (message.isNotEmpty())
                {
                    append(": $message - ")
                }
                append(ex.stackTraceToString())
            }

            fileLogger.Warn(formatted)
            platformConsole.Error(formatted)
        }
    }

    override fun CreateSpecificLogger(key: String): ILogging
    {
        val specLogger = ConditionalLogger(key, this, preferences)
        return specLogger
    }

    override fun Header(headerMessage: String)
    {
        SafeCall()
        {
            fileLogger.Info(headerMessage)
            platformConsole.Info(headerMessage)
        }
    }

    override fun LogMethodStarted(className: String, methodName: String, args: List<Any?>?)
    {
        SafeCall()
        {
            val debugMethodName = GetMethodNameWithParameters(className, methodName, args)
            Log("$ENTER_TAG $debugMethodName")
        }
    }

    override fun LogMethodStarted(methodName: String)
    {
        Log("$ENTER_TAG $methodName")
    }

    override fun LogMethodFinished(methodName: String)
    {
        Log("$EXIT_TAG $methodName")
    }

    override fun LogIndicator(name: String, message: String)
    {
        SafeCall()
        {
            val msg = "********************************${INDICATOR_TAG}${name}*************************************"
            fileLogger.Info(msg)
            platformConsole.Info(msg)
        }
    }

    override suspend fun GetSomeLogTextAsync(): String
    {
        val lines = fileLogger.GetLogListAsync()
        return lines.joinToString("\n")
    }

    override fun GetLogsFolder(): String
    {
        return fileLogger.GetLogsFolder()
    }

    override fun GetCurrentLogFileName(): String
    {
       return fileLogger.GetCurrentLogFileName()
    }

    override suspend fun GetLastSessionLogBytes(): ByteArray?
    {
        try
        {
            return fileLogger.GetCompressedLogsSync(true)
        }
        catch (ex: Exception)
        {
            LogError(ex, "Failed to get App Log")
            return null
        }
    }

    override suspend fun GetCompressedLogFileBytes(getOnlyLastSession: Boolean): ByteArray?
    {
        try
        {
            return fileLogger.GetCompressedLogsSync(getOnlyLastSession)
        }
        catch (ex: Exception)
        {
            LogError(ex, "Failed to get App Log")
            return null
        }
    }

    private fun GetLaunchCount(): Int
    {
        var launchCount = preferences.Get("AppLaunchCount", 0)
        if (launchCount != null)
            launchCount += 1
        else
            launchCount = 0

        preferences.Set("AppLaunchCount", launchCount)
        return launchCount
    }

    private fun GetLogAppTag(appLaunchCount: Int, rowNumber: Long): String
    {
        val now = Clock.System.now()
        val time = now.toLocalDateTime(TimeZone.currentSystemDefault())

        val millis = (time.nanosecond / 1_000_000) % 1000
        val millisStr = millis.toString().padStart(3, '0')

        val timeStr = "${time.hour.toString().padStart(2, '0')}:${
            time.minute.toString().padStart(2, '0')}:${
            time.second.toString().padStart(2, '0')}.$millisStr"

        return "S($appLaunchCount)_R($rowNumber)_D($timeStr)"
    }

    private fun SafeCall(action: ()-> Unit)
    {
        try
        {
            action()
        }
        catch (ex: Throwable)
        {
            platformConsole?.Error(ex.stackTraceToString())
        }
    }


    /**
     * Gets a method call with the class name, function name, and argument details.
     *
     * This function is designed to be **KMP-safe** (works in commonMain) and provides
     * a readable debug output for tracing method calls.
     *
     * Example output:
     * ```
     * Calling: MyClass.sampleFun(Int: 10, ArrayList[3] { 1, 2, 3 })
     * ```
     *
     * @param funcName The name of the function being called.
     *                 Typically passed as `::functionName.name`.
     * @param args     Optional list of arguments to log.
     *                 Supports:
     *                 - `Iterable<*>` (e.g., List, Set, etc.)
     *                 - `Array<*>`
     *                 - Any other type (logged as `TypeName: value`)
     * Notes:
     * - Collections are truncated to 10 items for readability.
     * - Safe for use in Kotlin Multiplatform (no reflection or JVM-only APIs).
     */
    private fun GetMethodNameWithParameters(className: String, funcName: String?, args: List<Any?>? = null) : String
    {
        //val className = this::class.simpleName
        val itemsCount = 10;
        val argsString = args?.joinToString(", ")
        { arg ->
            when (arg)
            {
                null -> "null"
                is Iterable<*> -> //is LIST
                {
                    val typeName = arg::class.simpleName ?: "Iterable"
                    val preview = arg.take(itemsCount).joinToString(", ") { it.toString() }
                    "$typeName[${arg.count()}] { $preview }, "
                }
                is Array<*> -> //ARRAY
                {
                    val typeName = arg::class.simpleName ?: "Array"
                    val preview = arg.take(itemsCount).joinToString(", ") { it.toString() }
                    "$typeName[${arg.size}] { $preview },"
                }
                else ->
                {
                    val typeName = arg::class.simpleName ?: "Any"

                    val valueString = when (arg)
                    {
                        // simple value-like types → print directly
                        is String,
                        is Number,
                        is Boolean,
                        is Char -> arg.toString()

                        // lists or arrays are handled above, so here we assume it’s an object
                        else ->
                        {
                            val defaultToStringPrefix = arg::class.simpleName ?: "Any"
                            val str = arg.toString()

                            // Kotlin/Native and Kotlin/JVM default toString() often looks like "ClassName@123abc"
                            if (str.startsWith(defaultToStringPrefix) && str.contains("@"))
                            {
                                "..." // looks like default toString()
                            } else
                            {
                                str // likely overridden
                            }
                        }
                    }

                    "$typeName: $valueString" //will get something like: Int: 5 or if type overrided toString Person: {Name: John}
                }
            }
        }

        return "$className.${funcName ?: "?"}($argsString)";
    }
}

class ConditionalLogger(private val key: String, private val logger: ILogging, private val preferences: IPreferences) : ILogging
{
    private val canLog: Boolean = preferences.Get(key, false)

    override fun Log(message: String)
    {
        if (canLog)
        {
            logger.Log(message)
        }
    }

    override fun LogWarning(message: String)
    {
        if (canLog)
        {
            logger.LogWarning(message)
        }
    }

    override fun LogMethodStarted(className: String, methodName: String, args: List<Any?>?)
    {
        if (canLog)
        {
            logger.LogMethodStarted(className, methodName, args)
        }
    }
}