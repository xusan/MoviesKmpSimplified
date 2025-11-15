package com.example.movieskmp

import com.example.movieskmp.base.BaseiOSRegistrar
import org.koin.core.module.Module

class AppiOSRegistrar
{
    companion object
    {
        fun RegisterTypes() : List<Module>
        {
            val baseIOSModule = BaseiOSRegistrar.RegisterTypes()
            val appCrosModules = AppCommonRegistrar.RegisterTypes()

            val mergedModules = baseIOSModule + appCrosModules;
            return mergedModules
        }
    }
}