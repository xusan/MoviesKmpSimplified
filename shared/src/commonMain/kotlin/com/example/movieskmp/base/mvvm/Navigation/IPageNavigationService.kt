package com.base.mvvm.Navigation

import com.base.mvvm.ViewModels.PageViewModel


interface IPageNavigationService {
    suspend fun Navigate(
        name: String,
        parameters: INavigationParameters? = null,
        useModalNavigation: Boolean = false,
        animated: Boolean = true,
        wrapIntoNav: Boolean = false
    )

    suspend fun NavigateToRoot(parameters: INavigationParameters? = null)

    fun GetCurrentPage(): IPage?

    fun GetCurrentPageModel(): PageViewModel?

    fun GetRootPageModel(): PageViewModel?

    fun GetNavStackModels(): List<PageViewModel>

    val CanNavigateBack: Boolean
}




