package com.example.movieskmp.base.mvvm.Navigation

import com.base.mvvm.ViewModels.PageViewModel

interface IViewModelProvider
{
    fun FetchViewModel(id: String): PageViewModel?
}