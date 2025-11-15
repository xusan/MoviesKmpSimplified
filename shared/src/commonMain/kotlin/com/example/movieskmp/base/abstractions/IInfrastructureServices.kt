package com.base.abstractions

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IInfrastructureServices", exact = true)
interface IInfrastructureServices
{
    suspend fun Start()
    suspend fun Pause()
    suspend fun Resume()
    suspend fun Stop()
}