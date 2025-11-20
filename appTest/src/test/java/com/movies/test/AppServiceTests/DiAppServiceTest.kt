package com.movies.test.AppServiceTests

import com.app.abstraction.domain.Movie
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.Repository.IRepository
import com.example.movieskmp.AppCommonRegistrar
import com.example.movieskmp.domain.Infasructures.REST.IMovieRestService
import com.movies.test.Impl.MockLogger
import com.movies.test.Impl.MockRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

open class DiAppServiceTest : KoinTest
{
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setupKoin()
    {
        Dispatchers.setMain(testDispatcher)

        // Register fake or test implementations for DI
        val mockPreference= mockk<IPreferences>(relaxed = true)
        val mockFileLogerService = mockk<IFileLogger>(relaxed = true)
        val mockErrorTracking = mockk<IErrorTrackingService>(relaxed = true)
        //mock rest service
        val movie = Movie.Companion.Create("test rest1", "overview rest1", null);
        movie.Id = 1;
        val someMovies = listOf(movie)
        val mockMovieRestService = mockk<IMovieRestService>()
        coEvery { mockMovieRestService.GetMovieRestlist() } returns someMovies
        //mock prefference
        every { mockPreference.Get("AppLaunchCount", 0) } returns 0


        val appServiceModule = AppCommonRegistrar.Companion.RegisterAppService()
        val unitTestModule = module {
            //services
            single<IMovieRestService> { mockMovieRestService }
            single<IPreferences> { mockPreference }
            single<IFileLogger> { mockFileLogerService }
            single<IErrorTrackingService> { mockErrorTracking }
            single<IRepository<Movie>> { MockRepository() }
            single<ILoggingService> { MockLogger() }
            // single<IMovieService> { MoviesService() }
        }

        val mergedModules = listOf(appServiceModule, unitTestModule)
        startKoin {
            modules(mergedModules)
        }
    }

    @After
    fun tearDownKoin()
    {
        stopKoin()
        Dispatchers.resetMain()
    }
}