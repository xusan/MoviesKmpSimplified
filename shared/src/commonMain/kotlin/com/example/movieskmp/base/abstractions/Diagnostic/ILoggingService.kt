package com.base.abstractions.Diagnostic

interface ILoggingService
{
    var LastError: Throwable?
    val HasError: Boolean

    fun Log(message: String)
    fun LogWarning(message: String)
    fun Header(headerMessage: String)
    fun LogMethodStarted(className: String, methodName: String, args: List<Any?>? = null)
    fun LogMethodStarted(methodName: String);
    fun LogMethodFinished(methodName: String)
    fun LogIndicator(name: String, message: String)
    fun LogError(ex: Throwable, message: String = "", handled: Boolean = true)
    fun TrackError(ex: Throwable, data: Map<String, String>? = null)
    fun LogUnhandledError(ex: Throwable)
    suspend fun GetCompressedLogFileBytes(getOnlyLastSession: Boolean = false): ByteArray?
    suspend fun GetSomeLogTextAsync(): String
    fun GetLogsFolder(): String
    suspend fun GetLastSessionLogBytes(): ByteArray?
}


