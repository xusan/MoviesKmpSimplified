package com.base.abstractions.Essentials.Display

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents the rotation a device display can have.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "DisplayRotation", exact = true)
enum class DisplayRotation(val value: Int)
{
    /** Unknown display rotation. */
    Unknown(0),

    /** The device display is rotated 0 degrees. */
    Rotation0(1),

    /** The device display is rotated 90 degrees. */
    Rotation90(2),

    /** The device display is rotated 180 degrees. */
    Rotation180(3),

    /** The device display is rotated 270 degrees. */
    Rotation270(4)
}