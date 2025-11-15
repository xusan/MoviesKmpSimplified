package com.app.shared.ViewModels

import com.base.abstractions.UI.ISnackbarService
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.TypeQualifier
import kotlin.reflect.KClass

class TestViewModel : KoinComponent
{
    private val snackBarService: ISnackbarService by inject()

    @Throws(Throwable::class)
    fun testCallMethod() : Boolean
    {
        snackBarService.ShowInfo("Hi swift message from Kotlin!!")

        return true;
    }
}

