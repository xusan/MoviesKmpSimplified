package com.base.impl.Droid.Essentials.Utils

import android.content.Intent
import com.base.impl.Droid.Utils.CurrentActivity

internal object PlatformUtils
{
    fun IsIntentSupported(intent: Intent): Boolean {
        val pm = CurrentActivity.Instance.packageManager ?: return false
        return intent.resolveActivity(pm) != null
    }
}