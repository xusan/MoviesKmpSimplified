package com.example.movieskmp.domain.AppServices

import com.app.abstraction.main.AppService.Dto.MovieDto
import com.base.abstractions.AppService.Some

interface IMovieService
{
    suspend fun GetListAsync(count: Int = -1, skip: Int = 0, remoteList: Boolean = false): Some<List<MovieDto>>
    suspend fun GetById(id: Int): Some<MovieDto>;
    suspend fun AddAsync(name: String, overview: String, posterUrl: String?): Some<MovieDto>
    suspend fun UpdateAsync(dtoModel: MovieDto): Some<MovieDto>;
    suspend fun RemoveAsync(dtoModel: MovieDto): Some<Int>;


}