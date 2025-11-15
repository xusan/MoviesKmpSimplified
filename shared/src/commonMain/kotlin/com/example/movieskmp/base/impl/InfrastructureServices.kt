package com.base.impl

import com.base.abstractions.IInfrastructureServices
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.REST.RequestQueueList
import org.koin.core.component.inject

open class InfrastructureServices : LoggableService(), IInfrastructureServices
{
    private val restQueueService: RequestQueueList by inject()

    override suspend fun Start()
    {

    }

    override suspend fun Pause()
    {
        restQueueService.Pause();
    }

    override suspend fun Resume()
    {
        restQueueService.Resume();
    }

    override suspend fun Stop()
    {
        restQueueService.Release();
    }

}