package com.base.impl.Diagnostic

import com.base.abstractions.Diagnostic.IPlatformOutput

internal class StandartPlatformOutput : IPlatformOutput
{
    override fun Info(message: String)
    {
        println(message)
    }

    override fun Warn(message: String)
    {
        println(message)
    }

    override fun Error(message: String)
    {
        println(message)
    }

}