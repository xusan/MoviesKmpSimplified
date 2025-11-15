package com.base.abstractions.Diagnostic

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IPlatformOutput", exact = true)
interface IPlatformOutput
{
    fun Info(message: String)
    fun Warn(message: String)
    fun Error(message: String)
}