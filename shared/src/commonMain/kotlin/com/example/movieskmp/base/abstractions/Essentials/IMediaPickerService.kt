package com.base.abstractions.Essentials

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IMediaPickerService", exact = true)
interface IMediaPickerService {
    suspend fun GetPhotoAsync(options: MediaOptions = MediaOptions()): MediaFile?
    suspend fun TakePhotoAsync(options: MediaOptions = MediaOptions()): MediaFile?
}

enum class MediaSource { CAMERA, GALLERY }

class MediaOptions
{
    //val source: MediaSource = MediaSource.GALLERY,
    val includeBytes: Boolean = false
    val compress: Boolean = false
    val compressionQuality: Int = 95 // 0..100, used only if compress = true
    val maxWidth: Int? = null
    val maxHeight: Int? = null
    val saveToAppDirectory: Boolean = true
}



class MediaFile(
    val FilePath: String,
    val MimeType: String?,
    val ByteData: ByteArray? = null
)