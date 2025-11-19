package com.base.impl.Droid.Essentials.Utils

import android.content.Intent
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.abstractions.Diagnostic.ILogging

internal object PlatformUtils
{
    fun IsIntentSupported(intent: Intent, logger: ILogging): Boolean
    {
        logger.Log("PlatformUtils.IsIntentSupported(...)")
        val pm = CurrentActivity.Instance.packageManager ?: return false
        return intent.resolveActivity(pm) != null
    }
}