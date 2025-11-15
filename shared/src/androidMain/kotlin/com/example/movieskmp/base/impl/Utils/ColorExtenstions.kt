package com.base.impl.Droid.Utils


import android.graphics.Color
import com.base.abstractions.Common.XfColor

internal fun XfColor.ToAndroid(): Int
{
    return Color.valueOf(
        (255 * this.R).toInt().toByte().toUByte().toInt() / 255f,
        (255 * this.G).toInt().toByte().toUByte().toInt() / 255f,
        (255 * this.B).toInt().toByte().toUByte().toInt() / 255f,
        (255 * this.A).toInt().toByte().toUByte().toInt() / 255f
    ).toArgb()
}


