package com.base.abstractions.Diagnostic

interface ILogging
{
    fun Log(message: String)
    fun LogWarning(message: String)
    fun LogMethodStarted(className: String, methodName: String, args: List<Any?>? = null)
}

interface ILoggingService : ILogging
{
    var LastError: Throwable?
    val HasError: Boolean

    fun LogMethodStarted(methodName: String)
    fun Header(headerMessage: String)
    fun LogMethodFinished(methodName: String)
    fun LogIndicator(name: String, message: String)
    fun LogError(ex: Throwable, message: String = "", handled: Boolean = true)
    fun TrackError(ex: Throwable, data: Map<String, String>? = null)
    fun LogUnhandledError(ex: Throwable)
    suspend fun GetCompressedLogFileBytes(getOnlyLastSession: Boolean = false): ByteArray?
    suspend fun GetSomeLogTextAsync(): String
    fun GetLogsFolder(): String
    fun GetCurrentLogFileName(): String
    suspend fun GetLastSessionLogBytes(): ByteArray?
    fun CreateSpecificLogger(key: String): ILogging
}

object SpecificLoggingKeys
{
    const val LogEssentialServices = "LogEssentialServices"
    const val LogUIServices = "LogUIServices"
    const val LogUIControlsKey = "LogUIControlsKey"
    const val LogUIPageKey = "LogUIPageKey"
    const val LogUINavigationKey = "LogUINavigationKey"
    const val LogUITableCells = "LogUITableCells"
}