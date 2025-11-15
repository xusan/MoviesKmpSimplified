package com.base.mvvm.ViewModels

import com.base.abstractions.Event

open class BindableBase : InjectableViewModel()
{
    // Event for property changed notifications
    val PropertyChanged = Event<String>()

    //Raises the PropertyChanged event for a given property.
    protected fun RaisePropertyChanged(propertyName: String)
    {
        PropertyChanged.Invoke(propertyName)
    }

    protected fun <T> SetProperty(propName: String, oldValue: T, newValue: T, assign: (T) -> Unit): Boolean
    {
        if (oldValue == newValue) return false
        assign(newValue)
        RaisePropertyChanged(propName)
        return true
    }

    //USAGE*******************************
//    var Title: String = ""
//        set(value) {
//            setProperty(::title.name, field, value) { field = it }
//        }
}

