package com.movies.test.IntegrationTests

import com.app.shared.ViewModels.AddEditMoviePageViewModel
import com.app.shared.ViewModels.MoviesPageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class MovieIntegrationTest : NavigableTest()
{
    @Test
    fun T1_1TestMainPageLoad() = runBlocking()
    {
        Navigate(MoviesPageViewModel::class.simpleName!!);
        delay(7000)
        val mainVm = GetNextPage<MoviesPageViewModel>();
        //validate
        assertTrue(mainVm.MovieItems.Count() > 0, "No movie items");
        EnsureNoError();
    }

    @Test
    fun T1_2TestAddMoview() = runBlocking()
    {
        Navigate(MoviesPageViewModel::class.simpleName!!);
        delay(1000);
        var mainVm = GetNextPage<MoviesPageViewModel>();
        val oldMovieCount = mainVm.MovieItems.Count();

        mainVm.AddCommand.ExecuteAsync();
        //navigated to create page
        val createMovieVm = GetNextPage<AddEditMoviePageViewModel>();
        createMovieVm.Model?.Name = "integration test movie 1";
        createMovieVm.Model?.Overview = "just testing integration test";
        //create movie
        createMovieVm.SaveCommand.ExecuteAsync();
        EnsureNoError();
        //navigated back to main page
        mainVm = GetNextPage<MoviesPageViewModel>();
        val newCount = mainVm.MovieItems.Count();
        //validate
        assertTrue(newCount == oldMovieCount + 1, "The old items count should increase to one item");
        EnsureNoError();
    }
}