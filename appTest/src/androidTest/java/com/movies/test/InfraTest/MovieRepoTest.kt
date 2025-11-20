package com.movies.test.InfraTest

import com.app.abstraction.domain.Movie
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Repository.IRepository
import kotlinx.coroutines.runBlocking
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.test.get
import java.lang.IllegalStateException
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MovieRepoTest : DeviceInfrastructureTest()
{
    companion object
    {
        var newId = 1;
    }

    @Test
    fun T1_1AddMovieTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        val movieEntity = Movie.Create("test movie from unittest", "good movie", "no url")
        movieRepo.AddAsync(movieEntity);
        assertTrue(movieEntity.Id > 0, "new movieEntity id doesn't increment")
        newId = movieEntity.Id;

        val logger = get<ILoggingService>();
        val filePath = logger.GetLogsFolder()
        logger.Log("App Log folder: $filePath")
    }

    @Test
    fun T1_2AddAllMovieTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        val movieEntity = Movie.Create("test movie from unittest", "good movie", "no url")
        val movieEntity2 = Movie.Create("test movie from unittest2", "good movie2", "no url2")
        movieRepo.AddAllAsync(listOf(movieEntity, movieEntity2));
        assertTrue(movieEntity.Id > 0, "new first id doesn't increment")
        assertTrue(movieEntity2.Id > 0, "new second movie id doesn't increment")
    }

    @Test
    fun T1_3GetListTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        val allList = movieRepo.GetListAsync();

        assertTrue { allList.count() > 0 }
    }

    @Test
    fun T1_4GetMovieTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        val movieEntity = movieRepo.FindById(newId);

        assertTrue(movieEntity != null, "FindById() returned null")
        assertTrue(movieEntity.Id > 0, "entity has incorrect id")
    }

    @Test
    fun T1_5UpdateMovieTest()
    {
        runBlocking() {
            val movieRepo = get<IRepository<Movie>>();
            val movieEntity = movieRepo.FindById(newId);
            if (movieEntity != null)
            {
                movieEntity.Name = "updated name"
                movieRepo.UpdateAsync(movieEntity);
            }
            else
            {
                throw IllegalStateException("Can not find movie entity")
            }
        }
    }

    @Test
    fun T1_6DeleteMovieTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        var entity = movieRepo.FindById(1);
        assertTrue(entity != null, "Can not find entity")
        val deletedCount = movieRepo.RemoveAsync(entity!!);
        assertTrue { deletedCount > 0 }

        entity = movieRepo.FindById(1);
        assertTrue(entity == null, "Entity was not removed")
    }

    @Test
    fun T1_7ClearAllMovieTest() = runBlocking()
    {
        val movieRepo = get<IRepository<Movie>>();
        movieRepo.ClearAsync("test clear");
        val list = movieRepo.GetListAsync()
        assertTrue (list.count() == 0, "table still has data after clear()")
    }

}