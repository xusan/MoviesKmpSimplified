package com.app.impl.cross.Infasructures.Repository

import com.app.abstraction.domain.Movie
import com.app.impl.cross.Infasructures.Repository.Tables.MovieTb
import com.base.impl.Repository.BaseRepository

internal class MovieRepository : BaseRepository<Movie, MovieTb>(MovieTb::class)
{

}