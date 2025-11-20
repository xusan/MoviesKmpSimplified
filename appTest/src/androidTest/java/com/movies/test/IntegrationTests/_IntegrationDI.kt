package com.movies.test.IntegrationTests

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.app.shared.Base.PageInjectedServices
import com.app.shared.ViewModels.AddEditMoviePageViewModel
import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.IConstant
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.ViewModels.PageViewModel
import com.example.movieskmp.AppDroidRegistrar
import com.movies.test.Impl.ConstImpl
import com.movies.test.Impl.MockPageNavigationService
import com.movies.test.TestActivity
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

open class _IntegrationDI: KoinTest
{

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setupKoin()
    {
        //NOTE
        //The DI Registration must be done inside Activity's onCreate method
        //Otherwise one of the service will crash (MediaPickerService)
        TestActivity.RegisterDI = { activity ->

            CurrentActivity.SetActivity(activity);
            CurrentActivity.SetContext(activity.applicationContext);

            val appDroidImpl = AppDroidRegistrar.RegisterTypes()

            val mockErrorTracking = mockk<IErrorTrackingService> (relaxed = true)
            val appModule = module()
            {
                single<IErrorTrackingService> {  mockErrorTracking  }
                single<IPageNavigationService> { MockPageNavigationService() }
                single<PageInjectedServices> {PageInjectedServices()}
                single<IConstant> { ConstImpl() }
                factory<PageViewModel>(named(MoviesPageViewModel::class.simpleName!!)) { MoviesPageViewModel(get()) }
                factory<PageViewModel>(named(AddEditMoviePageViewModel::class.simpleName!!)) { AddEditMoviePageViewModel(get()) }
            }
            val mergedModules = appDroidImpl + appModule
            startKoin {
                modules(mergedModules)
            }
        }
        //start activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        //launch TestActivity, which will call RegisterDI method to register DI services
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDownKoin()
    {
        stopKoin()
        scenario.close()
    }
}