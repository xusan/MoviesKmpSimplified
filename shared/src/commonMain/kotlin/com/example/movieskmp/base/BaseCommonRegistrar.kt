package com.example.movieskmp.base

import com.base.abstractions.Diagnostic.IAppLogExporter
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IVersionTracking
import com.base.abstractions.Messaging.IMessagesCenter
import com.base.abstractions.Messaging.SimpleMessageCenter
import com.base.abstractions.REST.IAuthTokenService
import com.base.abstractions.REST.IRestClient
import com.base.impl.Diagnostic.AppLogExporter
import com.base.impl.Diagnostic.AppLoggingService
import com.base.impl.Diagnostic.VersionTrackingImplementation
import com.base.impl.REST.AuthTokenService
import com.base.impl.REST.RequestQueueList
import com.base.impl.REST.RestClient
import org.koin.core.module.Module
import org.koin.dsl.module

class BaseCommonRegistrar
{
    companion object
    {
        fun RegisterTypes() : List<Module> = listOf(
            RegisterCommon(),
            RegisterInfrastructureService())


        fun RegisterCommon(): Module = module()
        {
            single<ILoggingService> { AppLoggingService() }
            single<IMessagesCenter> { SimpleMessageCenter() }
            single<IVersionTracking> { VersionTrackingImplementation() }
            single<IAppLogExporter> { AppLogExporter() }
        }

        fun RegisterInfrastructureService(): Module = module()
        {
            single<IRestClient> { RestClient() }
            single<IMessagesCenter> { SimpleMessageCenter() }
            single { RequestQueueList(get()) }
            single<IAuthTokenService> { AuthTokenService() }
        }
    }
}
