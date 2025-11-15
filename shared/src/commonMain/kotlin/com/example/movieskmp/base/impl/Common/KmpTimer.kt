package com.base.impl.Common

import com.base.abstractions.BaseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

class KmpTimer(private val Interval: Duration)
{
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    val Elapsed = BaseEvent()
    val IsEnabled: Boolean
        get()
        {
            if(job == null)
                return false;
            else
                return job!!.isActive
        }

    fun Start()
    {
        if (IsEnabled == true)
            return

        job = coroutineScope.launch()
        {
            while (isActive)
            {
                delay(Interval)
                Elapsed.Invoke()
            }
        }
    }

    fun Stop()
    {
        job?.cancel()
        job = null
    }

//    fun Restart()
//    {
//        Stop()
//        Start()
//    }
}