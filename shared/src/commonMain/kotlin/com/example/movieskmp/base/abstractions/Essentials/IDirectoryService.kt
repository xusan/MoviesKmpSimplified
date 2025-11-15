package com.base.abstractions.Essentials

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IDirectoryService", exact = true)
interface IDirectoryService
{
    fun GetCacheDir(): String
    fun GetAppDataDir(): String
    fun IsExistDir(path: String): Boolean
    fun CreateDir(path: String)
}