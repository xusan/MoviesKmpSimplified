package com.base.impl.Droid.Essentials

import android.os.Handler
import android.os.Looper
import com.base.abstractions.Essentials.IDeviceThreadService

internal class DroidDeviceThreadService : IDeviceThreadService
{
    override fun BeginInvokeOnMainThread(action: () -> Unit)
    {
        Handler(Looper.getMainLooper()).post(action)
    }
}