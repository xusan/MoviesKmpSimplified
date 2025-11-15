package com.example.movieskmp

import com.example.movieskmp.base.BaseDroidRegistrar
import org.koin.core.module.Module

class AppDroidRegistrar
{
    companion object
    {
        fun RegisterTypes() : List<Module>
        {
            val baseDroidModules = BaseDroidRegistrar.RegisterTypes()
            val appCommonModules = AppCommonRegistrar.RegisterTypes()

            val mergedModules = baseDroidModules + appCommonModules;
            return mergedModules
        }
    }
}