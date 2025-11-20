package com.movies.test.ViewModelTests

import com.app.abstraction.main.AppService.Dto.MovieDto
import com.app.shared.Base.PageInjectedServices
import com.app.shared.ViewModels.AddEditMoviePageViewModel
import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.abstractions.AppService.Some
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IDirectoryService
import com.base.abstractions.Essentials.IMediaPickerService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.IInfrastructureServices
import com.base.abstractions.Messaging.IMessagesCenter
import com.base.abstractions.UI.IAlertDialogService
import com.base.abstractions.UI.ISnackbarService
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Navigation.IPageNavigationService
import com.example.movieskmp.domain.AppServices.IMovieService
import com.movies.test._Impl.MockEventAgregator
import com.movies.test._Impl.MockLogger
import com.movies.test._Impl.MockSnackebar
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

open class DiViewModel : KoinTest
{
    private val testDispatcher = StandardTestDispatcher()
    lateinit var globalKoin: Koin

    @Before
    fun setupKoin()
    {
        Dispatchers.setMain(testDispatcher)

        // Register fake or test implementations for DI with relaxed = true
        //relaxed = true - means that any method calls will be ignored or faked
        val mockNavigationService = mockk<IPageNavigationService>(relaxed = true)
        val mockInfraService = mockk<IInfrastructureServices> (relaxed = true);
        //val mockSnackBar = mockk<ISnackbarService> (relaxed = true)
        val mockMeidaPicker = mockk<IMediaPickerService> (relaxed = true)
        val mockAlertDialog = mockk<IAlertDialogService> (relaxed = true)
        val mockDirectory = mockk<IDirectoryService>()
        val mockErrorTracking = mockk<IErrorTrackingService>(relaxed = true)
        val mockPreference= mockk<IPreferences>(relaxed = true)
        val mockFileLogger= mockk<IFileLogger>(relaxed = true)

        //fake results for IMovieService
        val mockMovieService = mockk<IMovieService> (relaxed = true)
        val movies = listOf(
            MovieDto(Id = 1, Name = "Test movie1", Overview = "overview test1", PosterUrl = "")
        )
        //mock MovieService method
        val someMovies = Some.FromValue(movies)
        coEvery { mockMovieService.GetListAsync(-1,0, false) } returns someMovies
        coEvery { mockMovieService.GetListAsync(-1,0, true) } returns someMovies
        val newMovie = MovieDto(Id = 1, Name = "Test movie1", Overview = "test overview1", PosterUrl = "")
        coEvery { mockMovieService.AddAsync("Test movie1","test overview1", "") } returns Some.FromValue(newMovie)
        //mock prefrences
        every { mockPreference.Get("AppLaunchCount", 0) } returns 0

        val appKoin = startKoin {
            modules(
                module {
                    //services
                    single<IPageNavigationService> {  mockNavigationService }
                    single<IInfrastructureServices> {  mockInfraService }
                    single<IAlertDialogService> {  mockAlertDialog }
                    single<IMediaPickerService> {  mockMeidaPicker }
                    single<IMovieService> {  mockMovieService }
                    single<IDirectoryService> {  mockDirectory }
                    single<IErrorTrackingService> {  mockErrorTracking }
                    single<IPreferences> {  mockPreference }
                    single<IFileLogger> {  mockFileLogger }
                    single<ISnackbarService> { MockSnackebar() }
                    single<IMessagesCenter> { MockEventAgregator() }
                    single<ILoggingService> { MockLogger() }

                    //viewmodels
                    single<PageInjectedServices> {  PageInjectedServices() }
                    factory<MoviesPageViewModel> { MoviesPageViewModel(get()) }
                    factory<AddEditMoviePageViewModel> { AddEditMoviePageViewModel(get()) }
                }
            )
        }

        AsyncCommand.DisableDoubleClickCheck = true;
        AsyncCommand.loggingService = get<ILoggingService>()

        //you can make globalKoin static field and resolve any type
        //var userRepo = globalKoin.get<IUserRepository>()
        globalKoin = appKoin.koin;
    }

    @After
    fun tearDownKoin()
    {
        stopKoin()
        Dispatchers.resetMain()
    }
}