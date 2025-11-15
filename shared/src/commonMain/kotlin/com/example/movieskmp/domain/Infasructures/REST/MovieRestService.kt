package com.example.movieskmp.domain.Infasructures.REST

import com.app.abstraction.domain.Movie
import com.base.abstractions.REST.RestRequest
import com.base.impl.REST.RestService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class MovieRestService : RestService(), IMovieRestService
{
    val baseImageHost = "https://image.tmdb.org/t/p/w300/";
    override suspend fun GetMovieRestlist() : List<Movie>
    {
        LogMethodStart(::GetMovieRestlist.name)

        val result = Get<MovieListResponse>(RestRequest(
            ApiEndpoint = "movie/popular?api_key=424f4be6472e955cadf36e104d8762d7",
            WithBearer = false))

        val list = result.Movies.map()
        { s ->
            val posterUrl = if (s.PosterPath.startsWith("/"))
            {
                val path = s.PosterPath.substring(1)
                baseImageHost + path
            }
            else ""

            Movie().apply()
            {
                Id = s.Id
                Name = s.Name
                Overview = s.Overview
                PosterUrl = posterUrl
            }
        }

        return list;
    }

}

@Serializable
class MovieListResponse(
    @SerialName("page")
    val Page: Int,
    @SerialName("total_pages")
    val TotalPages: Int,
    @SerialName("total_results")
    val TotalResults: Int,
    @SerialName("results")
    val Movies: List<MovieRestModel>
)

@Serializable
class MovieRestModel(
    @SerialName("id")
    val Id: Int,
    @SerialName("title")
    val Name: String,
    @SerialName("poster_path")
    val PosterPath: String = "",
    @SerialName("overview")
    val Overview: String = ""
)