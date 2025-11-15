package com.example.movieskmp.domain.AppServices

import com.app.abstraction.domain.Movie
import com.app.abstraction.main.AppService.Dto.MovieDto
import com.app.impl.cross.Map.ToDto
import com.app.impl.cross.Map.ToEntity
import com.base.abstractions.AppService.Some
import com.base.abstractions.Repository.IRepository
import com.base.impl.Diagnostic.LoggableService
import com.example.movieskmp.domain.Infasructures.REST.IMovieRestService
import org.koin.core.component.inject

internal class MoviesService : LoggableService(), IMovieService
{
    val movieRepository: IRepository<Movie> by inject()
    val movieRestService: IMovieRestService by inject()

    override suspend fun GetListAsync(count: Int, skip: Int, remoteList: Boolean): Some<List<MovieDto>>
    {
        try
        {
            LogMethodStart(::GetListAsync.name, count, skip, remoteList);

            var canLoadLocal: Boolean = true;
            var localList: List<Movie>? = null;
            if (remoteList)
            {
                canLoadLocal = false;
            }
            else
            {
                localList = this.movieRepository.GetListAsync();
                canLoadLocal = localList.count() > 0;
            }

            if (canLoadLocal)
            {
                loggingService.Log("MoviesService.GetListAsync(): loading from Local storage because canLoadLocal: $canLoadLocal")
                val dtoList = localList!!.map {s -> s.ToDto<MovieDto>()}.toList();
                return Some.FromValue(dtoList);
            }
            else
            {
                loggingService.Log("MoviesService.GetListAsync(): loading from Remote server because canLoadLocal: $canLoadLocal")
                //download all list
                val remoteList = movieRestService.GetMovieRestlist();
                val deletedCount = movieRepository.ClearAsync("${MoviesService::class.simpleName}: Delete all items requested when syncing");
                val insertedCount = movieRepository.AddAllAsync(remoteList);
                loggingService.Log("MoviesService.GetListAsync(): Sync completed deletedCount: $deletedCount, insertedCount: $insertedCount")

                //return dto list
                val dtoList = remoteList.map{s -> s.ToDto<MovieDto>()}.toList();
                return Some.FromValue(dtoList);
            }
        }
        catch (ex: Throwable)
        {
            return Some.FromError(ex);
        }
    }

    override suspend fun GetById(id: Int): Some<MovieDto>
    {
        try
        {
            LogMethodStart(::GetById.name, id);

            val movie = movieRepository.FindById(id);
            val dtoMovie = movie!!.ToDto<MovieDto>();
            return Some.FromValue(dtoMovie);
        }
        catch(ex: Throwable)
        {
            return Some.FromError(ex);
        }
    }

    override suspend fun AddAsync(name: String, overview: String, posterUrl: String?): Some<MovieDto>
    {
        try
        {
            LogMethodStart(::AddAsync.name, name,overview,posterUrl);

            val movie = Movie.Create(name, overview, posterUrl);
            this.movieRepository.AddAsync(movie);

            val dtoMovie = movie.ToDto<MovieDto>();
            return Some.FromValue(dtoMovie);
        }
        catch (ex: Throwable)
        {
            return Some.FromError(ex);
        }
    }

    override suspend fun UpdateAsync(dtoModel: MovieDto): Some<MovieDto>
    {
        try
        {
            LogMethodStart(::AddAsync.name, dtoModel);

            val movie = dtoModel.ToEntity<Movie>();
            this.movieRepository.UpdateAsync(movie);

            return Some.FromValue(dtoModel);
        }
        catch (ex: Throwable)
        {
            return Some.FromError(ex);
        }
    }

    override suspend fun RemoveAsync(dtoModel: MovieDto): Some<Int>
    {
        try
        {
            LogMethodStart(::RemoveAsync.name, dtoModel);

            val movie = dtoModel.ToEntity<Movie>();
            val res = this.movieRepository.RemoveAsync(movie);

            return Some.FromValue(res);
        }
        catch (ex: Throwable)
        {
            return Some.FromError(ex);
        }
    }
}

