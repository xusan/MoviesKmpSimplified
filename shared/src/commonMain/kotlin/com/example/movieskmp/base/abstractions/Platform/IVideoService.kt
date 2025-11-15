package com.base.abstractions.Platform

import com.base.abstractions.Common.Size
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IVideoService", exact = true)
interface IVideoService
{
    suspend fun getThumbnail(videoFilePath: String): ThumbnailInfo
    suspend fun compressVideo(inputPath: String): String
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "ThumbnailInfo", exact = true)
data class ThumbnailInfo(val imageSize: Size = Size.Zero, val filePath: String? = null)
{
    val Success: Boolean
        get() = !filePath.isNullOrEmpty()

    val IsPortrait: Boolean
        get() = if (imageSize.Width > 0) imageSize.Height > imageSize.Width else true
}