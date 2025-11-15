package com.base.impl.REST

import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.REST.Priority
import com.base.abstractions.REST.TimeoutType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


internal open class RequestQueueItem()
{
    var startedAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())

    var parentList: RequestQueueList? = null;
    var Id: String = "";
    var RequestAction: (suspend () -> String)? = null;
    var priority: Priority = Priority.NORMAL;
    val CompletionSource: CompletableDeferred<String> = CompletableDeferred();
    var timeoutType: TimeoutType = TimeoutType.Medium;
    var logger: ILoggingService? = null;
    var IsCompleted: Boolean = false
    var IsRunning: Boolean = false
    var result: String? = null

    val TimeOut: Int
        get() = when (timeoutType)
        {
            TimeoutType.High -> TimeoutType.High.value + 5
            TimeoutType.VeryHigh -> TimeoutType.VeryHigh.value + 5
            else -> TimeoutType.Medium.value + 1
        }

    val isTimeOut: Boolean
        get()
        {
            if (IsCompleted)
                return false

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val elapsedSeconds = now.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                    startedAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds
            return elapsedSeconds > TimeOut
        }

    suspend fun RunRequest()
    {
        try
        {
            IsRunning = true
            startedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            result = RequestAction?.invoke()

            if (!CompletionSource.isCancelled)
            {
                CompletionSource.complete(result ?: "")
            }
            else
            {
                logger?.LogWarning("RequestQueueItem: Skip setting result for Id:$Id because CompletionSource was cancelled")
            }

            IsCompleted = true
            IsRunning = false
        }
        catch (ex: Throwable)
        {
            ForceToComplete(ex, "Id:$Id Failed to invoke RequestAction()")
        }
        RemoveFromParent()
    }

    fun ForceToComplete(error: Throwable, logString: String)
    {
        if (IsCompleted)
        {
            logger?.LogWarning("No need to force complete the request $Id because it is already completed")
            return
        }

        IsRunning = false
        IsCompleted = true
        CompletionSource.completeExceptionally(error)
        logger?.LogError(error, logString)
    }

    fun RemoveFromParent()
    {
        try
        {
            parentList?.let()
            {
                if(it.contains(this))
                {
                    it.remove(this)
                    it.OnItemCompleted(this)
                }
            }
            parentList = null
        }
        catch (ex: Throwable)
        {
            logger?.LogError(ex, "Failed to remove item $Id from parent list")
        }
    }
}