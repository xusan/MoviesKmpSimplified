package com.app.shared.ViewModels

import com.app.shared.Base.AppPageViewModel
import com.app.shared.Base.PageInjectedServices
import com.app.shared.Events.MovieCellItemUpdatedEvent
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.base.abstractions.Messaging.GetEvent
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.NavigationParameters
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "MovieDetailPageViewModel", exact = true)//We need it to generate an exact name like MovieDetailPageViewModel. By default, it will generate MovieDetailPageViewModel and this can cause issue for navigation as the page is registered for the "MovieDetailPageViewModel" key
class MovieDetailPageViewModel(injectedService: PageInjectedServices) : AppPageViewModel(injectedService)
{
    companion object
    {
        const val PhotoChangedEvent: String = "PhotoChanged";
    }

    lateinit var EditCommand: AsyncCommand;
    var Model: MovieItemViewModel? = null
        set(value)
        {
            SetProperty(::Model.name, field, value) { field = it }
        }


    init
    {
        EditCommand = AsyncCommand(MainThreadScope,::OnEditCommand)
    }

    override fun Initialize(parameters: INavigationParameters)
    {
        LogMethodStart(::Initialize.name)
        super.Initialize(parameters)

        if (parameters.ContainsKey(MoviesPageViewModel.SELECTED_ITEM))
        {
            this.Model = parameters.GetValue<MovieItemViewModel>(MoviesPageViewModel.SELECTED_ITEM);
        }
    }

    override fun OnNavigatedTo(parameters: INavigationParameters)
    {
        LogMethodStart(::OnNavigatedTo.name)
        super.OnNavigatedTo(parameters)

        if (parameters.ContainsKey(AddEditMoviePageViewModel.UPDATE_ITEM))
        {
            this.Model = parameters.GetValue<MovieItemViewModel>(AddEditMoviePageViewModel.UPDATE_ITEM);
            val updateCellEvent = Services.EventAggregator.GetEvent<MovieCellItemUpdatedEvent> {MovieCellItemUpdatedEvent()}
            updateCellEvent.Publish(this.Model);

            RaisePropertyChanged(AddEditMoviePageViewModel.UPDATE_ITEM)
        }
    }

    suspend fun OnEditCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnEditCommand.name, arg)
            this.Navigate(AddEditMoviePageViewModel::class.simpleName!!, NavigationParameters
            {
               add(MoviesPageViewModel.SELECTED_ITEM, Model)
            })
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex);
        }
    }
}