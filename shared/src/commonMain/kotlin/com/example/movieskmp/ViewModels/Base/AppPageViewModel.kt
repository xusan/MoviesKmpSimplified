package com.app.shared.Base

import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.ViewModels.PageViewModel

open class AppPageViewModel(injectedService: PageInjectedServices) : PageViewModel(injectedService)
{
    //Commands
    var RefreshCommand: AsyncCommand;
    var Services: PageInjectedServices;

    //ctor
    init
    {
        Services = injectedService;
        RefreshCommand = AsyncCommand(MainThreadScope, ::OnRefreshCommand);
    }

    var IsRefreshing: Boolean = false
        set(value)
        {
            SetProperty(::IsRefreshing.name, field, value) { field = it }
        }

    protected open suspend fun OnRefreshCommand(arg: Any?)
    {
        LogMethodStart(::OnRefreshCommand.name, listOf(arg) )
    }



}