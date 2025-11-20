package com.movies.test.ViewModelTests

import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.abstractions.Diagnostic.ILoggingService
import kotlinx.coroutines.test.runTest
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.test.get
import org.koin.test.inject
import kotlin.getValue
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MoviesPageViewModelTest : DiViewModel()
{
    //private val mainVm by inject<MoviesPageViewModel>()
    private val loggingService by inject<ILoggingService>()

    @Test
    fun T1_1TestLoadMethod()= runTest {

        val mainVm = get<MoviesPageViewModel>()
        mainVm.LoadData();
        assertTrue { mainVm.MovieItems.Items.any()}
        assertFalse { loggingService.HasError }
    }

    @Test
    fun T1_2TestNavigateToCreateProduct() = runTest {

        val mainVm = get<MoviesPageViewModel>()
        mainVm.AddCommand.ExecuteAsync()
        assertFalse { loggingService.HasError }
    }

    @Test
    fun T1_3TestPullRefresh() = runTest {

        val mainVm = get<MoviesPageViewModel>()
        mainVm.RefreshCommand.ExecuteAsync()
        assertFalse { loggingService.HasError }
    }

}