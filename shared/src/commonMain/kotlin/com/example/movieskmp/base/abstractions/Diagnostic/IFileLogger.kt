package com.base.abstractions.Diagnostic

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IFileLogger", exact = true)
interface IFileLogger
{
    fun Init()
    fun Info(message: String)
    fun Warn(message: String)
    fun Error(message: String)
    suspend fun GetCompressedLogsSync(getOnlyLastSession: Boolean): ByteArray?
    suspend fun GetLogListAsync(): List<String>
    fun GetLogsFolder(): String
}