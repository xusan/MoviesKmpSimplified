package com.base.impl.Droid.Utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.lang.Math

object ContextExtensions {
    // Caching this display density here means that all pixel calculations are going to be based on the density
    // of the first Context these extensions are run against. That's probably fine, but if we run into a
    // situation where subsequent activities can be launched with a different display density from the intial
    // activity, we'll need to remove this cached value or cache it in a Dictionary<Context, float>
    private var s_displayDensity = Float.MIN_VALUE

    private fun SetupMetrics(context: Context) {
        if (s_displayDensity != Float.MIN_VALUE)
            return

        s_displayDensity = context.resources.displayMetrics.density
    }

    fun Context.FromPixels(pixels: Double): Double {
        SetupMetrics(this)

        return pixels / s_displayDensity
    }

    fun Context.ToPixels(dp: Double): Float {
        SetupMetrics(this)

        return Math.ceil(dp * s_displayDensity).toFloat()
    }

    fun Context.HideKeyboard(view: View?) {
        if (view == null)
            return

        val service = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        // service may be null in the context of the Android Designer
        if (service != null)
            service.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Context.ShowKeyboard(view: View) {
        val service = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        if (service != null)
            service.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }



    fun Context.TargetSdkVersion(): Int =
        this.applicationInfo.targetSdkVersion



}