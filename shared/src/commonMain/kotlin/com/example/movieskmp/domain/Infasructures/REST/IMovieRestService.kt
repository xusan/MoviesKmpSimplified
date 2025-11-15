package com.example.movieskmp.domain.Infasructures.REST

import com.app.abstraction.domain.Movie

interface IMovieRestService
{
    suspend fun GetMovieRestlist(): List<Movie>
}