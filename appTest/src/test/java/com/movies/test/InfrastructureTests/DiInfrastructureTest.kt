package com.movies.test.InfrastructureTests

import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.IConstant
import com.example.movieskmp.AppCommonRegistrar
import com.example.movieskmp.base.BaseCommonRegistrar
import com.movies.test._Impl.ConstImpl
import com.movies.test._Impl.MockLogger
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

open class DiInfrastructureTest : KoinTest
{
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setupKoin()
    {
        Dispatchers.setMain(testDispatcher)
        val mockPreference= mockk<IPreferences>(relaxed = true)
        val mockFileLogerService = mockk<IFileLogger>(relaxed = true)
        val mockErrorTracking = mockk<IErrorTrackingService>(relaxed = true)
        //mock prefference
        every { mockPreference.Get<Int>("AppLaunchCount", 0) } returns 0
        every { mockPreference.Get<String>("user_at", "") } returns ""

        val baseInfra = BaseCommonRegistrar.Companion.RegisterInfrastructureService()
        val appInfra = AppCommonRegistrar.Companion.RegisterInfrastructureService()

        val appModule = module() {
            single<IPreferences> { mockPreference }
            single<IFileLogger> { mockFileLogerService }
            single<IErrorTrackingService> { mockErrorTracking }
            single<ILoggingService> { MockLogger() }
            //single<IDirectoryService> { DirectoryService() }
            single<IConstant> { ConstImpl() }
        }
        startKoin {
            modules(baseInfra, appInfra, appModule)
        }
    }



    @After
    fun tearDownKoin()
    {
        stopKoin()
        Dispatchers.resetMain()
    }
}