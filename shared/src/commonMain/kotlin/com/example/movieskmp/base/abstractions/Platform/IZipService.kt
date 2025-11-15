package com.base.abstractions.Platform

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IZipService", exact = true)
interface IZipService
{
    suspend fun CreateFromDirectoryAsync(fileDir: String, zipPath: String)
    suspend fun ExtractToDirectoryAsync(filePath: String, dir: String, overwrite: Boolean)
    suspend fun CreateFromFileAsync(filePath: String, zipPath: String)
}