package com.base.mvvm.ViewModels

import com.base.mvvm.Navigation.INavigationAware
import com.base.mvvm.Navigation.INavigationParameters

open class NavigatingBaseViewModel(val injectedServices: InjectedService) : BaseViewModel(), INavigationAware
{

    val CanGoBack: Boolean
        get() = this.injectedServices?.NavigationService?.CanNavigateBack ?: false

    override fun OnNavigatedFrom(parameters: INavigationParameters)
    {

    }

    override fun OnNavigatedTo(parameters: INavigationParameters)
    {

    }

    fun GetCurrentPageViewModel() : NavigatingBaseViewModel
    {
        return this.injectedServices.NavigationService.GetCurrentPageModel() as NavigatingBaseViewModel;
    }

    suspend fun Navigate(name: String, parameters: INavigationParameters? = null, useModalNavigation: Boolean = false, animated: Boolean = true, wrapIntoNav: Boolean = false)
    {
        this.injectedServices.NavigationService.Navigate(name,parameters,useModalNavigation,animated,wrapIntoNav)
    }

    suspend fun NavigateToRoot(parameters: INavigationParameters? = null)
    {
        this.injectedServices.NavigationService.NavigateToRoot(parameters)
    }

    suspend fun SkipAndNavigate(skipCount: Int, route: String, parameters: INavigationParameters? = null)
    {
        var skip: String = ""
        for (i in 0 until skipCount) //for (int i = 0; i < skipCount; i++)
        {
            skip += "../"
        }
        val newRoute = "$skipCount$route";
        this.Navigate(newRoute, parameters);
    }

    suspend fun NavigateAndMakeRoot(name: String, parameters: INavigationParameters? = null, useModalNavigation: Boolean = false, animated: Boolean = true)
    {
        val newRoot = "/NavigationPage/$name";
        this.Navigate(name, parameters,useModalNavigation, animated);
    }

    suspend fun NavigateBack(parameters: INavigationParameters? = null)
    {
        Navigate("../", parameters);
    }

    suspend fun BackToRootAndNavigate(name: String, parameters: INavigationParameters? = null)
    {
        val navStack = injectedServices.NavigationService.GetNavStackModels()
            .map { it.toString().substringAfterLast('.') }

        val currentNavStack = if (navStack.size > 1)
        {
            navStack.joinToString("/")
        }
        else
        {
            navStack.firstOrNull().orEmpty()
        }

        // Find out how much we should go back to get root page
        val popCount = navStack.size - 1
        val popPageUri = if (popCount > 0) "../".repeat(popCount) else ""

        val resultUri = "$popPageUri$name"

        injectedServices.LoggingService.Log("BackToRootAndNavigate(): Current navigation stack: /$currentNavStack, pop count: $popCount, resultUri: $resultUri")

        Navigate(resultUri, parameters)
    }

    protected fun <T> GetParameter(parameters: INavigationParameters, key: String) : T?
    {
        if (parameters.ContainsKey(key))
        {
            return parameters.GetValue<T>(key);
        }
        else
        {
            return null;
        }
    }

    protected fun <T> GetParameter(parameters: INavigationParameters, key: String, setter: (T?)-> Unit)
    {
        if (parameters.ContainsKey(key))
        {
            val value = parameters.GetValue<T>(key);
            setter(value)
        }
    }
}