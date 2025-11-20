package com.movies.test.AppServiceTests

import com.example.movieskmp.domain.AppServices.IMovieService
import kotlinx.coroutines.test.runTest
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.test.get
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MovieServiceTest : DiAppServiceTest()
{
    @Test
    fun T1_1AddMovieTest() = runTest() {
        val movieService: IMovieService = get();
        val result = movieService.AddAsync("first product", "test overview", "");

        assertTrue(result.Success, "IMoviesService.AddAsync() failed in T1_1AddMovieTest()");
    }

    @Test
    fun T1_2GetMovieListTest() = runTest() {
        val movieService: IMovieService = get();
        val result = movieService.GetListAsync();

        assertTrue(result.Success, "IMoviesService.GetListAsync() failed in T1_2GetMovieListTest()");
        assertTrue(result.ValueOrThrow.count() > 0, "Movie count is zero in T1_2GetMovieListTest()");
    }

    @Test
    fun T1_3GetMovieTest() = runTest() {
        val movieService: IMovieService = get();
        val result = movieService.GetById(1);
        assertTrue(result.Success, "IMoviesService.GetById() failed in T1_3GetMovieTest()");
    }

    @Test
    fun T1_4UpdateMovieTest() = runTest() {
        val movieService: IMovieService = get();
        val result = movieService.GetById(1);
        assertTrue(result.Success, "IMoviesService.GetById() failed in T1_3GetMovieTest()");

        val item = result.ValueOrThrow;
        item.Name = "updated name";
        item.Overview = "updated overview";
        item.PosterUrl = "updated poster";
        val updateResult = movieService.UpdateAsync(item);
        assertTrue(updateResult.Success, "IMoviesService.UpdateAsync() failed in T1_3UpdateMovieTest()");
    }

    @Test
    fun T1_5RemoveMovieTest() = runTest() {
        val movieService: IMovieService = get();
        val result = movieService.GetById(1);
        assertTrue(result.Success, "IMoviesService.GetById() failed in T1_3UpdateMovieTest()");

        val item = result.ValueOrThrow;
        val removeResult = movieService.RemoveAsync(item);
        assertTrue(removeResult.Success, "IMoviesService.RemoveAsync() failed in T1_3RemoveMovieTest()");
    }
}