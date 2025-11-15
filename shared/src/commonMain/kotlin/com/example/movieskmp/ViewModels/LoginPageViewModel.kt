package com.app.shared.ViewModels

import com.app.shared.Base.AppPageViewModel
import com.app.shared.Base.PageInjectedServices
import com.base.abstractions.Essentials.IPreferences
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Navigation.INavigationParameters
import org.koin.core.component.inject
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "LoginPageViewModel", exact = true) //We need it to generate an exact name like LoginPageViewModel. By default, it will generate SACLoginPageViewModel and this can cause issue for navigation as the page is registered for the "LoginPageViewModel" key
class LoginPageViewModel(injectedService: PageInjectedServices) : AppPageViewModel(injectedService)
{
    val preferenceServices: IPreferences by inject();
    companion object {
        const val LogoutRequest = "LogoutRequest"
        const val IsLoggedIn = "IsLoggedIn"
    }
    var SubmitCommand: AsyncCommand;

    init
    {
        SubmitCommand = AsyncCommand(MainThreadScope,::OnSubmitCommand);
    }

    override fun Initialize(parameters: INavigationParameters)
    {
        LogMethodStart(::Initialize.name)
        super.Initialize(parameters)


        if(parameters.ContainsKey(LogoutRequest))
        {
            //do log out
            preferenceServices.Set(IsLoggedIn, false);
        }
    }


    var Login: String = ""
        set(value)
        {
            SetProperty(::Login.name, field, value) { field = it }
        }

    var Password: String = ""
        set(value)
        {
            SetProperty(::Password.name, field, value) { field = it }
        }

    suspend fun OnSubmitCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnSubmitCommand.name)
            //throw ClassCastException("Test Error: Invalid username or password")
            preferenceServices.Set(IsLoggedIn, true);
            Services.NavigationService.Navigate("/${MoviesPageViewModel::class.simpleName}")
        }
        catch (ex: Exception)
        {
            Services.LoggingService.TrackError(ex)
        }
    }
}