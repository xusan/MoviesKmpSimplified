package com.base.abstractions.Essentials.Display

data class DisplayInfo(
    val Width: Double,
    val Height: Double,
    val Density: Double,
    val Orientation: DisplayOrientation,
    val Rotation: DisplayRotation,
    val RefreshRate: Float
)


