package com.movies.test._Impl

import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MockLogger : ILoggingService
{
    companion object{
        const val ENTER_TAG: String = "➡Enter"
        const val EXIT_TAG: String = "\uD83C\uDFC3Exit"
        const val INDICATOR_TAG: String = "⏱Indicator_"
    }
    override var LastError: Throwable? = null;
    override val HasError: Boolean
        get() = LastError != null

    private fun getFormattedDate() : String
    {
        val timestamp = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())

        val formatted = String.format("%02d:%02d:%02d:%06d",
            timestamp.hour,
            timestamp.minute,
            timestamp.second,
            timestamp.nanosecond / 1000 // microseconds
        )

        return formatted
    }

    override fun Log(message: String)
    {
        println("${getFormattedDate()}_$message")
    }

    override fun LogWarning(message: String)
    {
        println("${getFormattedDate()}WARNING: $message")
    }

    override fun LogError(ex: Throwable, message: String, handled: Boolean)
    {
        println("ERROR: ${message},💥Handled Exception: ${ex.stackTraceToString()}")
    }

    override fun TrackError(ex: Throwable, data: Map<String, String>?)
    {
        LastError = ex;
        println("💥Handled Exception: ${ex.stackTraceToString()}")
    }

    override fun LogUnhandledError(ex: Throwable)
    {
        LastError = ex;
    }

    override fun Header(headerMessage: String)
    {
        TODO("Not yet implemented")
    }

    override fun LogMethodStarted(className: String, methodName: String, args: List<Any?>?)
    {
        println("${ENTER_TAG} ${className}.${methodName}()")
    }

    override fun LogMethodStarted(methodName: String)
    {
        println("${ENTER_TAG} $methodName")
    }


    override fun LogMethodFinished(methodName: String)
    {
        println("${EXIT_TAG} $methodName")
    }

    override fun LogIndicator(name: String, message: String)
    {
        TODO("Not yet implemented")
    }



    override suspend fun GetCompressedLogFileBytes(getOnlyLastSession: Boolean): ByteArray
    {
        TODO("Not yet implemented")
    }

    override suspend fun GetSomeLogTextAsync(): String
    {
        TODO("Not yet implemented")
    }

    override fun GetLogsFolder(): String
    {
        TODO("Not yet implemented")
    }

    override fun GetCurrentLogFileName(): String
    {
        TODO("Not yet implemented")
    }

    override suspend fun GetLastSessionLogBytes(): ByteArray
    {
        TODO("Not yet implemented")
    }

    override fun CreateSpecificLogger(key: String): ILogging
    {
        TODO("Not yet implemented")
    }

}