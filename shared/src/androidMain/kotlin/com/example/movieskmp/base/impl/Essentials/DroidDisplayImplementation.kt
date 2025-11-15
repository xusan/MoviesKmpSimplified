package com.base.impl.Droid.Essentials

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_SQUARE
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import com.base.abstractions.Essentials.Display.*
import com.base.impl.Droid.Utils.CurrentActivity

internal class DroidDisplayImplementation : IDisplay
{
    override fun GetDisplayKeepOnValue(): Boolean
    {
        val window = CurrentActivity.Instance.window
        val flags = window.attributes.flags
        return flags and FLAG_KEEP_SCREEN_ON != 0
    }

    override fun SetDisplayKeepOnValue(keepOn: Boolean)
    {
        val window = CurrentActivity.Instance.window
        if (keepOn)
            window.addFlags(FLAG_KEEP_SCREEN_ON)
        else
            window.clearFlags(FLAG_KEEP_SCREEN_ON)
    }

    override fun GetDisplayInfo(): DisplayInfo
    {
        val displayMetrics = DisplayMetrics()
        val display = GetDefaultDisplay()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(displayMetrics)

        return DisplayInfo(
            displayMetrics.widthPixels.toDouble(),
            displayMetrics.heightPixels.toDouble(),
            displayMetrics.density.toDouble(),
            CalculateOrientation(),
            CalculateRotation(display),
            display?.refreshRate ?: 0f)
    }

    private fun CalculateRotation(display: Display?): DisplayRotation =
        when (display?.rotation)
        {
            Surface.ROTATION_270 -> DisplayRotation.Rotation270
            Surface.ROTATION_180 -> DisplayRotation.Rotation180
            Surface.ROTATION_90 -> DisplayRotation.Rotation90
            Surface.ROTATION_0 -> DisplayRotation.Rotation0
            else -> DisplayRotation.Unknown
        }

    private fun CalculateOrientation(): DisplayOrientation =
        when (CurrentActivity.Instance.resources?.configuration?.orientation)
        {
            ORIENTATION_LANDSCAPE -> DisplayOrientation.Landscape
            ORIENTATION_PORTRAIT -> DisplayOrientation.Portrait
            ORIENTATION_SQUARE -> DisplayOrientation.Portrait
            else -> DisplayOrientation.Unknown
        }

    private fun GetDefaultDisplay(): Display?
    {
        try
        {
            val service = CurrentActivity.Instance.getSystemService(Context.WINDOW_SERVICE)
            val windowManager = service as? WindowManager
            return windowManager?.defaultDisplay
        }
        catch (ex: Exception)
        {
            Log.d("DeviceDisplay", "Unable to get default display: $ex")
            return null
        }
    }
}