package com.base.abstractions.Diagnostic

import com.base.abstractions.Event
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IErrorTrackingService", exact = true)
interface IErrorTrackingService
{
    val OnServiceError: Event<Throwable>
    fun Initialize()
    fun TrackError(ex: Throwable, attachment: ByteArray? = null, additionalData: Map<String, String>? = null)
}