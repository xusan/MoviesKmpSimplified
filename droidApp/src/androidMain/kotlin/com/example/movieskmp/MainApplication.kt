package com.example.movieskmp

import android.app.Application
import com.example.movieskmp.Impl.SentryErrorTracking

class MainApplication : Application()
{
    lateinit var sentryErrorTracker: SentryErrorTracking
    override fun onCreate()
    {
        super.onCreate()

        sentryErrorTracker = SentryErrorTracking(this)
        sentryErrorTracker.Initialize()
        Instance = this
    }

    companion object
    {
        lateinit var Instance: MainApplication
    }

}