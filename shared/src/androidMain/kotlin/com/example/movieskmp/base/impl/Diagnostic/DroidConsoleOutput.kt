package com.base.impl.Droid.Diagnostic

import android.util.Log
import com.base.abstractions.Diagnostic.IPlatformOutput

internal class DroidConsoleOutput : IPlatformOutput
{
    val TAG = "AppLogger"
    override fun Info(message: String)
    {
        Log.i(TAG, message);
    }

    override fun Warn(message: String)
    {
        Log.w(TAG, message);
    }

    override fun Error(message: String)
    {
        Log.e(TAG, message);
    }
}