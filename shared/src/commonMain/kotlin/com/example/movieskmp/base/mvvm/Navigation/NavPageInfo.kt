package com.base.mvvm.Navigation

import com.base.mvvm.ViewModels.PageViewModel


data class NavPageInfo(
    val vmName: String,
    val createPageFactory: () -> IPage,
    val createVmFactory: () -> PageViewModel
)