package com.movies.test.ViewModelTests

import com.app.shared.ViewModels.AddEditMoviePageViewModel
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.UI.ISnackbarService
import com.base.abstractions.UI.SeverityType
import com.base.mvvm.Navigation.NavigationParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import org.koin.test.inject
import kotlin.getValue
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateMovieViewModelTest : DiViewModel()
{
    private val loggingService by inject<ILoggingService>()

    @Test
    fun T2_1TestCreateProduct()= runTest {

        val createVm = get<AddEditMoviePageViewModel>()
        val popupAlert = get<ISnackbarService>()
        createVm.Initialize(NavigationParameters())
        var errorCount = 0
        popupAlert.PopupShowed += { popupType ->

            if(popupType == SeverityType.Error)
                errorCount++;
        };
        createVm.SaveCommand.ExecuteAsync();
        delay(200);
        assertTrue(errorCount == 1, "failed: name validation");

        createVm.Model?.Name = "Test movie1"
        createVm.SaveCommand.ExecuteAsync();
        delay(200);
        assertTrue(errorCount == 2, "failed: Overview validation");

        createVm.Model?.Overview = "test overview1";
        createVm.Model?.PosterUrl = "";
        createVm.SaveCommand.ExecuteAsync();
        delay(200);
        assertTrue(errorCount == 2, "validation error");
        assertFalse(loggingService.HasError, "There is another error beside validation error, the exception: ${loggingService.LastError?.stackTraceToString()}");
    }
}