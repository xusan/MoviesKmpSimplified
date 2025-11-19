package com.base.mvvm.ViewModels

import com.base.abstractions.Messaging.GetEvent
import com.base.abstractions.REST.Exceptions.AuthExpiredException
import com.base.abstractions.REST.Exceptions.HttpConnectionException
import com.base.abstractions.REST.Exceptions.HttpRequestException
import com.base.abstractions.REST.Exceptions.ServerApiException
import com.base.abstractions.REST.HttpStatusCode
import com.base.mvvm.Events.AppPausedEvent
import com.base.mvvm.Events.AppResumedEvent
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Actuals.SharedDispatchers
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.IPageLifecycleAware
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.*

open class PageViewModel(val injectedService: InjectedService) : NavigatingBaseViewModel(injectedService), IPageLifecycleAware
{
    var InstanceId: String
    lateinit var appResumedEvent: AppResumedEvent;
    lateinit var appPausedEvent: AppPausedEvent;
    private val job = SupervisorJob()
    protected val BackgroundScope = CoroutineScope(SharedDispatchers.Default + job)
    protected val MainThreadScope = CoroutineScope(SharedDispatchers.Main + job)

    var BackCommand: AsyncCommand
    var Title: String = ""
        set(value)
        {
            SetProperty(::Title.name, field, value) { field = it }
        }
    var IsPageVisable: Boolean = false;
    var IsFirstTimeAppears: Boolean = true;
    var BusyLoading: Boolean = false
        set(value)
        {
            SetProperty(::BusyLoading.name, field, value) { field = it }
        }
    var DisableDeviceBackButton: Boolean = false


    init
    {
        InstanceId = uuid4().toString()
        BackCommand = AsyncCommand(MainThreadScope, ::OnBackCommand)
    }



    override fun Initialize(parameters: INavigationParameters)
    {
        LogVirtualBaseMethod(::Initialize.name)
        super.Initialize(parameters)

        appResumedEvent = injectedServices.EventAggregator.GetEvent<AppResumedEvent>{AppResumedEvent()};
        appPausedEvent = injectedServices.EventAggregator.GetEvent<AppPausedEvent>{AppPausedEvent()};
        appResumedEvent.Subscribe(::ResumedFromBackground);
        appPausedEvent.Subscribe(::PausedToBackground);
    }

    override fun OnAppearing()
    {
        LogVirtualBaseMethod(::OnAppearing.name)
        IsPageVisable = true;
        this.RaisePropertyChanged(::CanGoBack.name);

        if (IsFirstTimeAppears)
        {
            IsFirstTimeAppears = false;
            OnFirstTimeAppears();
        }
    }

    fun OnAppeared()
    {
        LogVirtualBaseMethod(::OnAppeared.name)
    }

    protected open fun OnFirstTimeAppears()
    {
        LogVirtualBaseMethod(::OnFirstTimeAppears.name)
    }



    override fun OnDisappearing()
    {
        LogVirtualBaseMethod(::OnDisappearing.name)
        IsPageVisable = false;
    }

    override fun Destroy()
    {
        LogVirtualBaseMethod(::Destroy.name)

        try
        {
            super.Destroy()
            appResumedEvent.Unsubscribe(::ResumedFromBackground);
            appPausedEvent.Unsubscribe(::PausedToBackground);

            job.cancel()
        }
        catch (ex: Throwable)
        {
            injectedServices.LoggingService.TrackError(ex)
        }
    }


    override fun ResumedFromBackground(arg: Any?)
    {
        LogVirtualBaseMethod(::ResumedFromBackground.name)
    }

    override fun PausedToBackground(arg: Any?)
    {
        LogVirtualBaseMethod(::PausedToBackground.name)
    }

    suspend fun OnBackCommand(arg: Any?)
    {
        injectedServices.LoggingService.Log("${this::class.simpleName}.OnBackCommand() (from base)");
        this.NavigateBack();
    }

    fun DoDeviceBackCommand()
    {
        MainThreadScope.launch {

            injectedServices.LoggingService.Log("${this::class.simpleName}.DoDeviceBackCommand() (from base)");
            if (DisableDeviceBackButton)
            {
                injectedServices.LoggingService.Log("Cancel ${this::class.simpleName}.DoDeviceBackCommand(): Ignore back command because this page is set to cancel any device back button.");
            }
            else
            {
                BackCommand?.ExecuteAsync();
            }
        }
    }

    suspend fun ShowLoading(asyncAction: suspend () -> Unit, onComplete: ((Boolean) -> Unit)? = null)
    {
        try
        {
            LogMethodStart(::ShowLoading.name)
            BusyLoading = true
            //run long-running operation in background thread
            withContext(SharedDispatchers.Default)
            {
                asyncAction()
            }
            onComplete?.invoke(true)
        }
        finally
        {
            BusyLoading = false
        }
    }

    suspend fun <T> ShowLoadingWithResult(asyncAction: suspend () -> T, setIsBusy: Boolean = true) : T
    {
        try
        {
            LogMethodStart("ShowLoadingWithResult")
            BusyLoading = setIsBusy
            //run long-running operation in background thread
            val result = withContext(SharedDispatchers.Default)
            {
                asyncAction()
            }
            return result
        }
        finally
        {
            BusyLoading = false
        }
    }

    /**
     * Note that: it executes action in Default/Background CoroutineDispatcher, so it will crash if it will contain UI related code
     *
     * @param backgroundActionAsync action that should be executed in background
     * @param setIsBusy value for BusyLoading
     */
    suspend fun ShowLoadingAndHandleErrorInBackground(backgroundActionAsync: suspend () -> Unit, setIsBusy: Boolean = true)
    {
        LogMethodStart(::ShowLoadingAndHandleErrorInBackground.name)
        try
        {
            // TODO: implement skipCheckInternet check when services are available
            BusyLoading = setIsBusy
            withContext(SharedDispatchers.Default)
            {
                backgroundActionAsync()
            }
        }
        catch (x: Exception)
        {
            HandleUIError(x)
        }
        finally
        {
            BusyLoading = false
        }
    }


    /**
     * Note that: it executes action in Default/Background CoroutineDispatcher, so it will crash if it will contain UI related code
     *
     * @param backgroundActionAsync action that should be executed in background
     * @param setIsBusy value for BusyLoading
     * @return The result of backgroundActionAsync or null if failed.
     */
    suspend fun <T> GetWithLoadingAndHandleError(backgroundActionAsync: suspend () -> T, setIsBusy: Boolean = true): T?
    {
        LogMethodStart("GetWithLoadingAndHandleError")
        try
        {
            BusyLoading = setIsBusy
            val result = withContext(SharedDispatchers.Default)
            {
                backgroundActionAsync()
            }
            return result
        }
        catch (x: Throwable)
        {
            HandleUIError(x)
            return null
        }
        finally
        {
            BusyLoading = false
        }
    }



    fun HandleUIError(x: Throwable)
    {
        LogMethodStart(::HandleUIError.name)

        var knownError = true
        if (x is CancellationException)
        {
            injectedServices.LoggingService.LogWarning("Ignoring the CancellationException")
            return
        }
        else if (x is AuthExpiredException || (x is HttpRequestException && x.statusCode == HttpStatusCode.Unauthorized.ordinal))
        {
            injectedServices.LoggingService.LogWarning("Skip showing error popup for user because this error is handled in main view ${x::class.simpleName}: ${x.message}")
            return
        }
        else if (x is HttpConnectionException)
        {
            injectedServices.SnackBarService.ShowError("It looks like there may be an issue with your connection. Please check your internet connection and try again.")
        }
        else if (x is HttpRequestException)
        {
            if (x.statusCode == HttpStatusCode.ServiceUnavailable.ordinal)
            {
                injectedServices.SnackBarService.ShowError("The server is temporarily unavailable. Please try again later.")
            }
            else
            {
                injectedServices.SnackBarService.ShowError("It seems server is not available, please try again later. (StatusCode - ${x.statusCode}).")
            }
        }
        else if (x is ServerApiException)
        {
            injectedServices.SnackBarService.ShowError("Internal Server Error. Please try again later.")
        }
        else
        {
            knownError = false
            injectedServices.SnackBarService.ShowError("Oops something went wrong, please try again later.")
        }

        if (knownError)
        {
            injectedServices.LoggingService.LogError(x);
        }
        else
        {
            injectedServices.LoggingService.TrackError(x);
        }
    }

//    protected fun LogVirtualBaseMethod(methodName: String)
//    {
//        injectedServices.LoggingService.Log("${this::class.simpleName}.${methodName}() (from base)");
//    }
}