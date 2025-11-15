package com.base.impl

import org.koin.core.Koin

class ContainerLocator
{
    companion object
    {
        var Container: Koin? = null

        inline fun <reified T: Any> Resolve() : T
        {
            return Container!!.get<T>();
        }
    }
}