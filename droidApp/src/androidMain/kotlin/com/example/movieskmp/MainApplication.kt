package com.example.movieskmp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity
import com.example.movieskmp.Impl.SentryErrorTracking

class MainApplication : Application()
{
    lateinit var sentryErrorTracker: SentryErrorTracking
    override fun onCreate()
    {
        super.onCreate()

        try
        {
            sentryErrorTracker = SentryErrorTracking(this)
            sentryErrorTracker.Initialize()
            registerActivityLifecycleCallbacks(AppLifecycle)
            Instance = this
            CurrentActivity.SetContext(this.applicationContext)

            val bootstrap = Bootstrap()
            bootstrap.RegisterTypes(this.applicationContext)

            val loggingService = ContainerLocator.Resolve<ILoggingService>()
            loggingService.Log("####################################################- APPLICATION STARTED -####################################################");
            loggingService.Log("MainApplication.OnCreate()");
        }
        catch (ex: Throwable)
        {
            val loggingService = ContainerLocator.Resolve<ILoggingService>()
            loggingService.LogError(ex)
            throw ex
        }

    }

    companion object
    {
        lateinit var Instance: MainApplication
    }
}

object AppLifecycle : Application.ActivityLifecycleCallbacks
{

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?)
    {
        CurrentActivity.SetActivity(activity as AppCompatActivity)
    }

    override fun onActivityStarted(activity: Activity)
    {
        CurrentActivity.SetActivity(activity as AppCompatActivity)
    }

    override fun onActivityResumed(activity: Activity)
    {
        CurrentActivity.SetActivity(activity as AppCompatActivity)
    }

    override fun onActivityPaused(activity: Activity)
    {
        if (CurrentActivity.Instance === activity)
        {
            CurrentActivity.Clear()
        }
    }

    override fun onActivityStopped(activity: Activity)
    {
        if (CurrentActivity.Instance === activity)
        {
            CurrentActivity.Clear()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity)
    {
        if (CurrentActivity.Instance === activity)
        {
            CurrentActivity.Clear()
        }
    }
}