package com.movies.test.InfrastructureTests

import com.example.movieskmp.domain.Infasructures.REST.IMovieRestService
import kotlinx.coroutines.test.runTest
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.test.get
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MovieRestServiceTest : DiInfrastructureTest()
{
    @Test
    fun T1_1TestGetMovies() = runTest() {
        val movieRestService = get<IMovieRestService>()
        val list = movieRestService.GetMovieRestlist()
        assertTrue { list.any() }
        assertTrue { list.first().PosterUrl.contains("http") }
    }
}