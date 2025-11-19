package com.base.impl.Diagnostic

import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class LoggableService : KoinComponent
{
    val loggingService: ILoggingService by inject()
    lateinit var specificLogger: ILogging
    var specificLoggerInitialized = false

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

    fun InitSpecificlogger(key: String)
    {
        if(specificLoggerInitialized == false)
        {
            specificLogger = loggingService.CreateSpecificLogger(key)
            specificLoggerInitialized = true
        }
    }

    fun SpecificLogMethodStart(methodName: String, vararg args: Any? )
    {
        try
        {
            val className = this::class.simpleName!!
            specificLogger.LogMethodStarted(className, methodName, args.toList())
        }
        catch (ex: Throwable)
        {
            println(ex.stackTraceToString())
        }
    }
}