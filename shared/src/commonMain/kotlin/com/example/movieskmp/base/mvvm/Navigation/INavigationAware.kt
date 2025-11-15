package com.base.mvvm.Navigation

interface INavigationAware
{
    //Called when the implementer has been navigated away from.
    fun OnNavigatedFrom(parameters: INavigationParameters)

    //Called when the implementer has been navigated to.
    fun OnNavigatedTo(parameters: INavigationParameters)
}