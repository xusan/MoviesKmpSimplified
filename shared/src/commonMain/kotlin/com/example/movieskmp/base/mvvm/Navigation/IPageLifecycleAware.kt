package com.base.mvvm.Navigation

interface IPageLifecycleAware
{
    fun OnAppearing()
    fun OnDisappearing()

    fun ResumedFromBackground(arg: Any?)
    fun PausedToBackground(arg: Any?)
}