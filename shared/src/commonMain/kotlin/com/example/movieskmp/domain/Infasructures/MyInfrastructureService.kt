package com.app.impl.cross.Infasructures

import com.base.abstractions.Repository.ILocalDbInitilizer
import com.base.impl.InfrastructureServices
import org.koin.core.component.inject

internal class MyInfrastructureService : InfrastructureServices()
{
    val dbInitilizer : ILocalDbInitilizer by inject()

    override suspend fun Start()
    {
        LogMethodStart(::Start.name);
        super.Start()
        dbInitilizer.Init()
    }

    override suspend fun Pause()
    {
        LogMethodStart(::Pause.name);
        super.Pause()
    }

    override suspend fun Resume()
    {
        LogMethodStart(::Resume.name);
        super.Resume()
    }

    override suspend fun Stop()
    {
        LogMethodStart(::Stop.name);
        super.Stop()
        dbInitilizer.Release()
    }

}