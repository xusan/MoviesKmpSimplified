package com.base.abstractions.Platform

import com.base.abstractions.Common.Size
import kotlin.experimental.ExperimentalObjCName
import kotlin.math.*
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IResizeImageService", exact = true)
interface IResizeImageService {

    fun ResizeImage(
        imageData: ByteArray,
        originalContentType: String,
        maxWidth: Int,
        maxHeight: Int,
        quality: Float = 97f,
        rotation: Int = 0,
        shouldSetUniqueName: Boolean = false
    ): ImageResizeResult

    fun ResizeNativeImage(
        image: Any,
        originalContentType: String,
        maxWidth: Int,
        maxHeight: Int,
        rotation: Int = 0,
        quality: Float = 97f,
        shouldSetUniqueName: Boolean = false
    ): ImageResizeResult

    fun GetRequiredRotation(fileResult: Any): Int
    fun GetRequiredRotation(filePath: String): Int
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "ImageResizeResult", exact = true)
data class ImageResizeResult(
    var IsResized: Boolean = true,
    var NativeImage: Any? = null,
    var Image: ByteArray? = null,
    var ContentType: String = "",
    var ImageSize: Size = Size.Zero,
    var FilePath: String? = null)
{
    val FileExtension: String
        get() = if (ContentType.contains("png", ignoreCase = true)) ".png" else ".jpg"

    val IsPortrait: Boolean
        get() = ImageSize == Size.Zero || ImageSize.Height > ImageSize.Width
}

