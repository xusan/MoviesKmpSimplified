package com.base.mvvm.ViewModels
import com.base.abstractions.BaseEvent
import com.base.mvvm.Navigation.IDestructible
import com.base.mvvm.Navigation.IInitialize
import com.base.mvvm.Navigation.INavigationParameters

open class BaseViewModel : BindableBase(), IDestructible, IInitialize
{
    val Initialized = BaseEvent()
    val OnDestroyed = BaseEvent()

    override fun Initialize(parameters: INavigationParameters)
    {
        Initialized.Invoke()
    }

    override fun Destroy()
    {
        OnDestroyed.Invoke()
    }
}