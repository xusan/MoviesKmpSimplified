package com.app.shared.ViewModels

import com.app.abstraction.main.AppService.Dto.MovieDto
import com.app.shared.Base.AppPageViewModel
import com.app.shared.Base.PageInjectedServices
import com.app.shared.Events.MovieCellItemUpdatedEvent
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.base.abstractions.Diagnostic.IAppLogExporter
import com.base.abstractions.IInfrastructureServices
import com.base.abstractions.Messaging.GetEvent
import com.base.abstractions.REST.AuthErrorEvent
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Helpers.ObservableCollection
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.NavigationParameters
import org.koin.core.component.inject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.base.mvvm.Helpers.ToDebugString
import com.example.movieskmp.domain.AppServices.IMovieService
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "MoviesPageViewModel", exact = true)//We need it to generate an exact name like MoviesPageViewModel. By default, it will generate MoviesPageViewModel and this can cause issue for navigation as the page is registered for the "MoviesPageViewModel" key
class MoviesPageViewModel(injectedService: PageInjectedServices) : AppPageViewModel(injectedService)
{
    val TAG: String = "${MoviesPageViewModel::class.simpleName}:"
    companion object
    {
        const val SELECTED_ITEM = "SELECTED_ITEM"
    }
    private val appLogExporter: IAppLogExporter by inject()
    private val movieService: IMovieService by inject()
    private val infrastructureServices: IInfrastructureServices by inject()
    private var movieCellUpdatedEvent: MovieCellItemUpdatedEvent
    private var authErrorEvent: AuthErrorEvent
    var MenuTappedCommand: AsyncCommand;
    var AddCommand: AsyncCommand;
    var ItemTappedCommand: AsyncCommand;
    var MovieItems = ObservableCollection<MovieItemViewModel>()
        set(value)
        {
            SetProperty(::MovieItems.name, field, value) { field = it }
        }

    init
    {
        MenuTappedCommand = AsyncCommand(MainThreadScope,::OnMenuTappedCommand)
        AddCommand = AsyncCommand(MainThreadScope,::OnAddCommand)
        ItemTappedCommand = AsyncCommand(MainThreadScope,::OnItemTappedCommand)

        movieCellUpdatedEvent = Services.EventAggregator.GetEvent<MovieCellItemUpdatedEvent>{ MovieCellItemUpdatedEvent() }
        authErrorEvent = Services.EventAggregator.GetEvent<AuthErrorEvent> { AuthErrorEvent() }
        movieCellUpdatedEvent.Subscribe(::OnMovieCellItemUpdatedEvent)
        authErrorEvent.Subscribe (::HandleAuthErrorEvent)


    }

    override fun Initialize(parameters: INavigationParameters)
    {
        super.Initialize(parameters)

        MainThreadScope.launch()
        {
            //init infrastructure services (ie local storage, rest api)
            infrastructureServices.Start()
            //try to load data
            LoadData()
        }
    }

    override fun OnNavigatedTo(parameters: INavigationParameters)
    {
        super.OnNavigatedTo(parameters)
        try
        {
            if(parameters.ContainsKey(AddEditMoviePageViewModel.NEW_ITEM))
            {
                val newProduct = GetParameter<MovieItemViewModel>(parameters, AddEditMoviePageViewModel.NEW_ITEM)!!
                MovieItems.Add(0,newProduct)
            }
            else if(parameters.ContainsKey(AddEditMoviePageViewModel.REMOVE_ITEM))
            {
                val removedItem = GetParameter<MovieItemViewModel>(parameters, AddEditMoviePageViewModel.REMOVE_ITEM)
                if(removedItem != null)
                {
                    MovieItems.Remove(removedItem);
                }
            }
        }
        catch (ex: Throwable)
        {
            Services.LoggingService.TrackError(ex)
        }
    }

    override fun PausedToBackground(arg: Any?)
    {
        BackgroundScope.launch()
        {
            try
            {
                infrastructureServices.Pause()
            }
            catch (ex: Throwable)
            {
                Services.LoggingService.TrackError(ex)
            }
        }
    }

    override fun ResumedFromBackground(arg: Any?)
    {
        BackgroundScope.launch()
        {
            try
            {
                infrastructureServices.Resume()
            }
            catch (ex: Throwable)
            {
                Services.LoggingService.TrackError(ex)
            }
        }

    }

    override fun Destroy()
    {
        val job = BackgroundScope.launch()
        {
            try
            {
                movieCellUpdatedEvent.Unsubscribe(::OnMovieCellItemUpdatedEvent)
                authErrorEvent.Unsubscribe(::HandleAuthErrorEvent)
                infrastructureServices.Stop()
            }
            catch (ex: Throwable)
            {
                Services.LoggingService.TrackError(ex)
            }
        }

        //call destroy for base method
        job.invokeOnCompletion()
        {
            super.Destroy()//Do not call base destroy before ViewModelScope.launch completes because base.Destroy cancels the ViewModelScope
        }
    }

    suspend fun OnAddCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnAddCommand.name)
            Navigate(AddEditMoviePageViewModel::class.simpleName!!)
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex)
        }
    }

    suspend fun OnItemTappedCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnItemTappedCommand.name, arg)
            val item = arg as? MovieItemViewModel ?: return
            Navigate(MovieDetailPageViewModel::class.simpleName!!,NavigationParameters
                {
                    add(SELECTED_ITEM, item)
                })
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex)
        }
    }

    suspend fun OnMenuTappedCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnMenuTappedCommand.name, arg)
            val item = arg as MenuItem
            if (item.Type == MenuType.ShareLogs)
            {
                val res = appLogExporter.ShareLogs()
                if(!res.Success)
                {
                    val error = res.Exception?.message
                    injectedServices.SnackBarService.ShowError("Share file failed. $error")
                }
            }
            else if (item.Type == MenuType.Logout)
            {
                val confirmed = Services.AlertDialogService.ConfirmAlert("Confirm Action", "Are you sure want to log out?", "Yes", "No")
                if (confirmed)
                {
                    Navigate("../${LoginPageViewModel::class.simpleName}", NavigationParameters
                    {
                        add(LoginPageViewModel.LogoutRequest, true)
                    })
                }
            }
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex)
        }
    }

    fun OnMovieCellItemUpdatedEvent(model: Any?)
    {
        LogMethodStart(::OnMovieCellItemUpdatedEvent.name, model)
        MainThreadScope.launch()
        {
            try
            {
                val movieItem = model as MovieItemViewModel;

                val oldItem = MovieItems.Items.firstOrNull { it.Id == movieItem.Id }
                oldItem?.let()
                {
                    val index = MovieItems.Items.indexOf(it)
                    if (index >= 0)
                    {
                        MovieItems.Replace(index, movieItem)
                    }
                }
            }
            catch (ex: Throwable)
            {
                Services.LoggingService.TrackError(ex)
            }
        }
    }

    override suspend fun OnRefreshCommand(arg: Any?)
    {
        LogMethodStart(::OnRefreshCommand.name)
        try
        {
            IsRefreshing = true

            LoadData(true, true)
        }
        catch (ex: Throwable)
        {
            Services.LoggingService.TrackError(ex)
        }
        finally
        {
            IsRefreshing = false
        }
    }

    private fun SetMovieList(list: List<MovieDto>)
    {
        val convertedList = list.map { MovieItemViewModel(it) }.toMutableList()
        Services.LoggingService.Log("${TAG}: setting data to MovieItems property from OnRefreshCommand result: ${list.ToDebugString()}");
        MovieItems = ObservableCollection(convertedList)
    }

    private val semaphoreAuthError = Semaphore(1)
    var loggingOut: Boolean = false
    protected fun HandleAuthErrorEvent(arg: Any?)
    {
        LogMethodStart(::HandleAuthErrorEvent.name)

        MainThreadScope.launch {
            try
            {
                semaphoreAuthError.withPermit {
                    if (loggingOut)
                    {
                        Services.LoggingService.LogWarning("Skip HandleAuthErrorEvent() because another thread is handling it");
                        return@withPermit
                    }
                    loggingOut = true;

                    val currentVm = GetCurrentPageViewModel()
                    if (currentVm !is MoviesPageViewModel) currentVm.NavigateToRoot();

                    Navigate("../${LoginPageViewModel::class.simpleName}", NavigationParameters
                    {
                        add(LoginPageViewModel.LogoutRequest, true)
                    })
                }
            }
            catch (ex: Throwable)
            {
                Services.LoggingService.TrackError(ex);
            }
        }
    }

    suspend fun LoadData(getFromServer: Boolean = false, showError: Boolean = false)
    {
        LogMethodStart(::LoadData.name, getFromServer)

        val result = ShowLoadingWithResult({ movieService.GetListAsync(remoteList = getFromServer)}, getFromServer)
        if(result.Success)
        {
            SetMovieList(result.ValueOrThrow)
        }
        else
        {
            result.Exception?.let()
            {
                if(showError)
                    HandleUIError(it)
                else
                    loggingService.TrackError(it)
            }
        }
    }

}

class MenuItem
{
    var Title: String = ""
    var Icon: String = ""
    var Type: MenuType = MenuType.None
}

enum class MenuType
{
    None, Logout, ShareLogs
}