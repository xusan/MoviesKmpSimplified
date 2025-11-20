package com.movies.test.InfraTest

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Repository.ILocalDbInitilizer
import com.base.impl.Droid.Utils.CurrentActivity
import com.example.movieskmp.AppDroidRegistrar
import com.movies.test.TestActivity
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.get

open class DeviceInfrastructureTest : KoinTest
{
    private lateinit var scenario: ActivityScenario<TestActivity>
    //private val testDispatcher = StandardTestDispatcher()


    @Before
    fun setupKoin()
    {
        //NOTE
        //The DI Registration must be done inside Activity's onCreate method
        //Otherwise one of the service will crash (MediaPickerService)
        TestActivity.RegisterDI = { activity ->
            //set current activity (it is required by some UI services)
            CurrentActivity.SetActivity(activity)
            CurrentActivity.SetContext(activity.applicationContext)

            // Register your DI services here
            val mockErrorTracking = mockk<IErrorTrackingService> (relaxed = true)

            val appDroidModule = AppDroidRegistrar.RegisterTypes()
            val testModule = module()
            {
                single<IErrorTrackingService> {  mockErrorTracking  }
            }
            val mergedModules = appDroidModule + testModule

            startKoin {
                modules(mergedModules)
            }

            //init the database
            runBlocking {
                val dbInitializer = get<ILocalDbInitilizer>()
                dbInitializer.Init()
            }
        }

        //start activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        //launch TestActivity, which will call RegisterDI method to register DI services
        scenario = ActivityScenario.launch<TestActivity>(intent)
    }





    @After
    fun tearDownKoin()
    {
        stopKoin()
        //Dispatchers.resetMain()
        scenario.close()
    }
}