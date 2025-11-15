package com.base.abstractions.Essentials

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IDeviceThreadService", exact = true)
interface IDeviceThreadService
{
    fun BeginInvokeOnMainThread(action: () -> Unit)
}