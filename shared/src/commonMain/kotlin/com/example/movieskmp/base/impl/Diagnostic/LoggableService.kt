package com.base.impl.Diagnostic

import com.base.abstractions.Diagnostic.ILoggingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class LoggableService : KoinComponent
{
    val loggingService: ILoggingService by inject()

    fun LogMethodStart(methodName: String, vararg args: Any? )
    {
        try
        {
            val loggingService = get<ILoggingService>()
            val className = this::class.simpleName!!
            loggingService.LogMethodStarted(className, methodName, args.toList())
        }
        catch (ex: Throwable)
        {
            println(ex.stackTraceToString())
        }
    }
}