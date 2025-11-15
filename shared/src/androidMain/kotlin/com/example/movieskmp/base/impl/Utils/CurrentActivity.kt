package com.base.impl.Droid.Utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ComponentActivity

object CurrentActivity
{
    private var activity: AppCompatActivity? = null

    val Instance: AppCompatActivity
        get() { return activity!!}

    var AppContext: Context? = null

    fun SetActivity(componentActivity: AppCompatActivity)
    {
        activity = componentActivity;
        AppContext = componentActivity;
    }

    fun SetContext(context: Context)
    {
        AppContext = context
    }

}