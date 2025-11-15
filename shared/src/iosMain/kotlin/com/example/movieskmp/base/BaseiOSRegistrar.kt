package com.example.movieskmp.base

import com.base.abstractions.Diagnostic.IPlatformOutput
import com.base.impl.Diagnostic.StandartPlatformOutput
import org.koin.core.module.Module
import org.koin.dsl.module

class BaseiOSRegistrar
{
    companion object
    {
        fun RegisterTypes() : List<Module>
        {
            val baseiOSModule = module()
            {
                //Diagnostic
                single<IPlatformOutput> { StandartPlatformOutput() }
            }
            val baseCrossModule = BaseCommonRegistrar.RegisterTypes()

            //merge modules
            val list = baseiOSModule + baseCrossModule
            return list
        }
    }
}