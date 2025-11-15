package com.base.mvvm.ViewModels

import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IShare
import com.base.abstractions.Messaging.IMessagesCenter
import com.base.abstractions.UI.ISnackbarService
import com.base.mvvm.Navigation.IPageNavigationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class InjectedService : KoinComponent
{
    val NavigationService: IPageNavigationService by inject ()
    val EventAggregator: IMessagesCenter by inject ()
    val LoggingService: ILoggingService  by inject ()
    val SnackBarService: ISnackbarService by inject()

    //val shareService: IShare by inject()
}

//object DI {
//    val container get() = KoinContext.get()
//
//    inline fun <reified T> resolve(): T = container.get()
//}