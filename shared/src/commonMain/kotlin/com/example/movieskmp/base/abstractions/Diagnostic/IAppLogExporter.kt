package com.base.abstractions.Diagnostic

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IAppLogExporter", exact = true)
interface IAppLogExporter
{
    suspend fun ShareLogs(): LogSharingResult
}

class LogSharingResult(val Success: Boolean, val Exception: Exception? = null)