package com.example.movieskmp

import androidx.appcompat.app.AppCompatActivity
import com.app.shared.Base.PageInjectedServices
import com.app.shared.ViewModels.*
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.IConstant
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.Navigation.NavRegistrar
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.module
import com.example.movieskmp.Pages.Login.*
import com.example.movieskmp.Pages.Movies.*

class Bootstrap : KoinComponent
{
    constructor(componentActivity: AppCompatActivity)
    {
        CurrentActivity.SetActivity(componentActivity)
    }
    fun RegisterTypes(pageNavigationService: IPageNavigationService)
    {
        val appDroidImpl = AppDroidRegistrar.RegisterTypes()
        val pageRegistrar = NavRegistrar()
        val appModule = module()
        {
            //app services
            single<IConstant> { ConstantImpl() }
            single<IPageNavigationService> { pageNavigationService }
            single { PageInjectedServices() }
            single<IErrorTrackingService> { MainApplication.Instance.sentryErrorTracker }
            single<NavRegistrar> { pageRegistrar }
        }
        //register pages
        pageRegistrar.RegisterPageForNavigation<LoginPageViewModel, LoginPage>({ LoginPage()}, { LoginPageViewModel(get())})
        pageRegistrar.RegisterPageForNavigation<MoviesPageViewModel, MoviesPage>({ MoviesPage()}, { (MoviesPageViewModel(get()))})
        pageRegistrar.RegisterPageForNavigation<MovieDetailPageViewModel, MovieDetailPage>({ MovieDetailPage()}, { (MovieDetailPageViewModel(get()))})
        pageRegistrar.RegisterPageForNavigation<AddEditMoviePageViewModel, AddEditMoviePage>({ AddEditMoviePage()}, { (AddEditMoviePageViewModel(get()))})

        val mergedModules = appDroidImpl + appModule + pageRegistrar.modules;

        val koinApp = startKoin()
        {
            modules(mergedModules)
        }
        ContainerLocator.Container = koinApp.koin
    }

    suspend fun NavigateToPageAsync(pageNavigationService: IPageNavigationService)
    {
        val preference = get<IPreferences>()
        val isloggedIn = preference.Get(LoginPageViewModel.IsLoggedIn, false);

        if (isloggedIn!!)
        {
            pageNavigationService.Navigate("/${MoviesPageViewModel::class.simpleName}", animated = false);
        }
        else
        {
            pageNavigationService.Navigate("/${LoginPageViewModel::class.simpleName}", animated = false);
        }
    }
}