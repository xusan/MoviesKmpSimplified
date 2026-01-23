package com.app.shared.ViewModels

import com.app.shared.Base.AppPageViewModel
import com.app.shared.Base.PageInjectedServices
import com.app.shared.Events.MovieCellItemUpdatedEvent
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.base.abstractions.Messaging.GetEvent
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.NavigationParameters
import com.example.movieskmp.domain.AppServices.IMovieService
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.experimental.ExperimentalObjCName
import kotlin.getValue
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "MovieDetailPageViewModel", exact = true)//We need it to generate an exact name like MovieDetailPageViewModel. By default, it will generate MovieDetailPageViewModel and this can cause issue for navigation as the page is registered for the "MovieDetailPageViewModel" key
open class MovieDetailPageViewModel(injectedService: PageInjectedServices) : AppPageViewModel(injectedService)
{
    protected val movieService: IMovieService by inject()
    companion object
    {
        const val PhotoChangedEvent: String = "PhotoChanged";
    }

    var EditCommand: AsyncCommand;
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
        try
        {
            LogMethodStart(::Initialize.name)
            super.Initialize(parameters)

            if (parameters.ContainsKey(MoviesPageViewModel.SELECTED_ITEM))
            {
                val movieId = parameters.GetValue<Int>(MoviesPageViewModel.SELECTED_ITEM)!!
                MainThreadScope.launch { LoadMovie(movieId) }
            }
        }
        catch (ex: Throwable)
        {
            Services.LoggingService.TrackError(ex)
        }
    }

    override fun OnNavigatedTo(parameters: INavigationParameters)
    {
        try
        {
            LogMethodStart(::OnNavigatedTo.name)

            if (parameters.ContainsKey(AddEditMoviePageViewModel.UPDATE_ITEM))
            {
                Model?.let()
                {
                    MainThreadScope.launch()
                    {
                        LoadMovie(it.Id)
                        val updateCellEvent = Services.EventAggregator.GetEvent<MovieCellItemUpdatedEvent> {MovieCellItemUpdatedEvent()}
                        updateCellEvent.Publish(it.Id);

                        RaisePropertyChanged(AddEditMoviePageViewModel.UPDATE_ITEM)
                    }

                }

            }
        }
        catch (ex: Throwable)
        {
            Services.LoggingService.TrackError(ex)
        }
    }

    suspend fun OnEditCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnEditCommand.name, arg)
            if(Model == null)
            {
                Services.LoggingService.LogWarning("Ignore OnEditCommand because Model is null")
                return
            }

            this.Navigate(AddEditMoviePageViewModel::class.simpleName!!, NavigationParameters
            {
               add(MoviesPageViewModel.SELECTED_ITEM, Model!!.Id)
            })
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex);
        }
    }

    suspend fun LoadMovie(movieId: Int)
    {
        try
        {
            LogMethodStart(::LoadMovie.name, movieId)

            val result = movieService.GetById(movieId)
            if(result.Success)
            {
                Model = MovieItemViewModel(result.ValueOrThrow)
            }
        }
        catch (ex: Throwable)
        {
            Services.LoggingService.TrackError(ex)
        }
    }
}