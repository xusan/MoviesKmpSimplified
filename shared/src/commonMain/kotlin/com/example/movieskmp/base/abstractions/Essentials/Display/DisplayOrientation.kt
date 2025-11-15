package com.base.abstractions.Essentials.Display

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName


/**
 * Represents the orientation a device display can have.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "DisplayOrientation", exact = true)
enum class DisplayOrientation(val value: Int)
{
    /** Unknown display orientation. */
    Unknown(0),

    /** Device display is in portrait orientation. */
    Portrait(1),

    /** Device display is in landscape orientation. */
    Landscape(2)
}