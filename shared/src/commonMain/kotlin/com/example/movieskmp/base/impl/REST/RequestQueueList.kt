package com.base.impl.REST

import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Event
import com.base.abstractions.REST.Priority
import com.base.impl.Common.KmpTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration.Companion.seconds

internal class RequestQueueList(val loggingService: ILoggingService, private val items: MutableList<RequestQueueItem> = mutableListOf()) : MutableList<RequestQueueItem> by items
{
    var RequestStarted = Event<RequestQueueItem>();
    var RequestPending = Event<RequestQueueItem>();
    var RequestCompleted = Event<RequestQueueItem>();

    private val MaxBackgroundRequest = 1
    private val MaxHighPriority = 2
    private val queueSemaphor: Semaphore = Semaphore(1)
    private val timeOutTimer = KmpTimer(10.seconds)
    private val TAG = "${RequestQueueList::class.simpleName}"

    init
    {
        timeOutTimer.Elapsed += ::TimeOutTimer_Elapsed
    }

    fun TimeOutTimer_Elapsed()
    {
        loggingService.Log("$TAG: Time out timer tick elapsed to check request that time out.");
        CheckTimeOutRequest();
    }

    override fun add(element: RequestQueueItem) : Boolean
    {
        val isAdded = items.add(element)
        //run without await
        CoroutineScope(Dispatchers.Default).launch()
        {
            TryRunNextRequest()
        }
        ResumeTimer()

        return isAdded
    }

    fun Resume()
    {
        ResumeTimer()
    }

    fun Pause()
    {
        timeOutTimer.Stop();
    }

    fun Release()
    {
        timeOutTimer.Stop();
        items.clear()
    }

    suspend fun TryRunNextRequest(): Boolean
    {
        var canStart = false;
        try
        {
            queueSemaphor.withPermit()
            {
                val item = items.filter { !it.IsRunning && !it.IsCompleted }.minByOrNull { it.priority.ordinal }  // assuming Priority is enum

                item?.let { nextItem ->
                    val highPriorityRunning = items.count { it.priority == Priority.HIGH && it.IsRunning }

                    canStart = when
                    {
                        nextItem.priority == Priority.HIGH && highPriorityRunning < MaxHighPriority -> true
                        nextItem.priority != Priority.HIGH && highPriorityRunning == 0 && items.count { it.IsRunning } < MaxBackgroundRequest -> true
                        else -> false
                    }

                    if (canStart)
                    {
                        OnRequestStarted(nextItem)
                        //run without await
                        CoroutineScope(Dispatchers.Default).launch()
                        {
                            nextItem.RunRequest()
                        }
                    }
                    else
                    {
                        OnRequestPending(nextItem)
                    }
                }
            }
        }
        catch (ex: Throwable)
        {
            loggingService.TrackError(ex);
            return true;
        }
        return canStart;
    }

    fun OnItemCompleted(requestQueueItem: RequestQueueItem)
    {
        OnRequestCompleted(requestQueueItem)
        CoroutineScope(Dispatchers.Default).launch()
        {
            for (i in items.indices)
            {
                val valResult = TryRunNextRequest()
                if (!valResult)
                    break
            }
        }
    }

    private fun OnRequestStarted(e: RequestQueueItem)
    {
        try
        {
            loggingService.Log("$TAG: The next request ${e.Id} started. ${GetQueueInfo()}")
            RequestStarted.Invoke(e)
        }
        catch (ex: Throwable)
        {
            loggingService.LogError(ex, "")
        }
    }

    private fun OnRequestPending(item: RequestQueueItem?)
    {
        try
        {
            loggingService.LogWarning("$TAG: Waiting for running requests to complete. ${GetQueueInfo()}")
            item?.let { RequestPending.Invoke(it) }
        }
        catch (ex: Throwable)
        {
            loggingService.LogError(ex, "")
        }
    }

    private fun OnRequestCompleted(e: RequestQueueItem)
    {
        try
        {
            loggingService.Log("$TAG: The request ${e.Id} completed. ${GetQueueInfo()}")
            RequestCompleted.Invoke(e)
        }
        catch (ex: Exception)
        {
            loggingService.LogError(ex, "")
        }
    }

    private fun GetQueueInfo(): String
    {
        val totalCount = items.size
        val runningCount = items.count { it.IsRunning }
        val highPriorityCount = items.count { it.priority == Priority.HIGH }
        return "$TAG: Queue total count: $totalCount, running count: $runningCount, high priority count: $highPriorityCount"
    }

    private fun CheckTimeOutRequest()
    {
        if(items.count() == 0)
        {
            StopTimer()
            return
        }

        val timeOutList = items.filter { it.isTimeOut }

        if (timeOutList.isEmpty())
        {
            loggingService.Log("$TAG: No timeout requests, total items count: ${timeOutList.size}")
        }
        else
        {
            loggingService.LogWarning("$TAG: Found ${timeOutList.size} timeout items, removing them")
            timeOutList.forEach { requestItem ->
                requestItem.ForceToComplete(Exception("The request id:${requestItem.Id} is TIME OUT"), "$TAG: The request id:${requestItem.Id} is TIME OUT")
                requestItem.RemoveFromParent()
            }

            if (items.isEmpty())
            {
                loggingService.LogWarning("$TAG: No items to run (Count:0)")
                StopTimer()
            }
            else
            {
                loggingService.Log("$TAG: Calling TryRunNextRequest() to run next item, totalCount: ${items.size}")
                CoroutineScope(Dispatchers.Default).launch { TryRunNextRequest() }
            }
        }
    }

    fun ResumeTimer()
    {
        if (!timeOutTimer.IsEnabled)
        {
            loggingService.LogWarning("$TAG: Starting timer that checks time out request in the Queue list");
            timeOutTimer.Start();
        }
    }

    fun StopTimer()
    {
        loggingService.LogWarning("$TAG: Queue List is empty: stoping the timeout timer")
        timeOutTimer.Stop();
    }


}





