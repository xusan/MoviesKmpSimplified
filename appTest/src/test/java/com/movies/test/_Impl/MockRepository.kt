package com.movies.test._Impl

import com.app.abstraction.domain.Movie
import com.base.abstractions.Repository.IRepository

class MockRepository : IRepository<Movie>
{
    override suspend fun FindById(id: Int): Movie
    {
        return Movie.Create("test", "test overview", null)
    }

    override suspend fun GetListAsync(count: Int, skip: Int): List<Movie>
    {
        val list = listOf<Movie>(Movie.Create("test2", "test overview2", null))
        return list;
    }

    override suspend fun AddAsync(entity: Movie): Int
    {
        return 1
    }

    override suspend fun UpdateAsync(entity: Movie): Int
    {
        return 1
    }

    override suspend fun AddAllAsync(entities: List<Movie>): Int
    {
        return 1
    }

    override suspend fun RemoveAsync(entity: Movie): Int
    {
        return 1
    }

    override suspend fun ClearAsync(reason: String): Int
    {
        return 1
    }

}