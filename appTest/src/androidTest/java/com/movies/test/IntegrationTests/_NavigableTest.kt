package com.movies.test.IntegrationTests

import com.base.abstractions.Diagnostic.ILoggingService
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.ViewModels.PageViewModel
import org.koin.test.get

open class NavigableTest : _IntegrationDI()
{

    // Called after every test
    open fun LogOut()
    {
        LogMessage("***********TEST method ends****************")
    }

    protected fun GetCurrentPage(): PageViewModel?
    {
        val navigation = get<IPageNavigationService>()
        return navigation.GetCurrentPageModel()
    }

    protected fun EnsureNoError()
    {
        val log = get<ILoggingService>()
        if (log.HasError)
        {
            val exc = log.LastError
            log.LastError = null
            throw LogLastErrorException(exc?.message ?: "", exc!!)
        }
    }

    protected suspend fun Navigate(name: String)
    {
        val nav = get<IPageNavigationService>()
        nav.Navigate(name)
    }

    protected inline fun <reified CorrectPageT : PageViewModel> ThrowWrongPageError(wrongPage: PageViewModel)
    {
        val correctPageName = CorrectPageT::class.simpleName
        val wrongPageName = wrongPage::class.simpleName
        val error = "App should be navigated to $correctPageName but navigated to $wrongPageName."
        ThrowException(error)
    }

    protected inline fun <reified T : PageViewModel> GetNextPage(): T
    {
        EnsureNoError()
        val page = GetCurrentPage()
        if (page is T)
        {
            return page
        }
        else
        {
            ThrowWrongPageError<T>(page ?: throw Exception("No current page"))
            throw Exception("Unreachable") // keeps compiler happy
        }
    }

    // Helper placeholders (to be implemented in your test framework)
    protected fun LogMessage(message: String)
    {
        println(message)
    }

    protected fun ThrowException(message: String): Nothing
    {
        throw Exception(message)
    }
}

class LogLastErrorException(toString: String, exception: Throwable) : Exception(toString, exception)
{

}