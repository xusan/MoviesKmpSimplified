package com.app.impl.cross.Map

import com.app.abstraction.domain.Movie
import com.app.impl.cross.Infasructures.Repository.Tables.MovieTb
import com.base.abstractions.Repository.IRepoMapper

internal class RepoMovieMapper : IRepoMapper<Movie, MovieTb>
{
    override fun ToTb(entity: Movie): MovieTb
    {
        val movieTb = MovieTb().apply()
        {
            Id = entity.Id;
            Name = entity.Name;
            Overview = entity.Overview;
            PostUrl =  if(entity.PosterUrl.isNullOrEmpty()) "" else entity.PosterUrl!!
        }
        return movieTb;
    }

    override fun ToEntity(tb: MovieTb): Movie
    {
        val movieEntity = Movie.Create(tb.Name, tb.Overview, tb.PostUrl).apply()
        {
            Id = tb.Id;
        }
        return movieEntity;
    }

    override fun MoveData(from: Movie, to: MovieTb)
    {
        to.apply {
            Name = from.Name;
            Overview = from.Overview;
            PostUrl = from.PosterUrl
        }
    }
}