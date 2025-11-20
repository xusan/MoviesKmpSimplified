package com.movies.test.Impl

import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.IPage
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.Navigation.NavigationParameters
import com.base.mvvm.ViewModels.PageViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.util.Stack

class MockPageNavigationService : KoinComponent, IPageNavigationService
{
    private val ViewModelStackList: Stack<PageViewModel> = Stack()

    override val CanNavigateBack: Boolean
        get() = ViewModelStackList.size > 1

    override suspend fun Navigate(name: String, parameters: INavigationParameters?, useModalNavigation: Boolean, animated: Boolean, wrapIntoNav: Boolean)
    {
        GetCurrentPageModel()?.OnDisappearing()

        var newPageName = name

        when
        {
            name.contains("../") ->
            {
                val splitCount = name.split("/").size - 1
                repeat(splitCount) {
                    GetCurrentPageModel()?.Destroy()
                    if (ViewModelStackList.isNotEmpty()) ViewModelStackList.pop()
                }
                newPageName = name.replace("../", "")
            }

            name.contains("/") ->
            {
                ViewModelStackList.forEach { it.Destroy() }
                ViewModelStackList.clear()
                newPageName = name.replace("/", "")
            }
        }

        if (newPageName.isEmpty())
        {
            if (name.contains("../") && parameters != null && parameters.Count() > 0)
            {
                GetCurrentPageModel()?.OnNavigatedTo(parameters)
            }
            return
        }

        // Resolve directly from Koin using the registered name
        val viewModel = get<PageViewModel>(qualifier = named(newPageName))

        val params = parameters ?: NavigationParameters()
        viewModel.Initialize(params)
        viewModel.OnNavigatedTo(params)
        viewModel.OnAppearing()
        //viewModel.OnAppeared()

        GetCurrentPageModel()?.OnNavigatedFrom(NavigationParameters())

        ViewModelStackList.push(viewModel)
    }

    override suspend fun NavigateToRoot(parameters: INavigationParameters?)
    {
        while (ViewModelStackList.size > 1)
        {
            val current = ViewModelStackList.pop()
            current.Destroy()
        }
    }

    override fun GetCurrentPage(): IPage?
    {
        TODO("Not yet implemented")
    }


    override fun GetCurrentPageModel(): PageViewModel?
    {
        return ViewModelStackList.lastOrNull()
    }

    override fun GetRootPageModel(): PageViewModel?
    {
        TODO("Not yet implemented")
    }

    override fun GetNavStackModels(): List<PageViewModel>
    {
        return ViewModelStackList.toList()
    }


}