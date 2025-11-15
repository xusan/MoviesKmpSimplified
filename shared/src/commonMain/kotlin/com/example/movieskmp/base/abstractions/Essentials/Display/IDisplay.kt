package com.base.abstractions.Essentials.Display

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IDisplay", exact = true)
interface IDisplay
{
    fun GetDisplayInfo(): DisplayInfo
    fun GetDisplayKeepOnValue() : Boolean
    fun SetDisplayKeepOnValue(keepOn: Boolean)
}