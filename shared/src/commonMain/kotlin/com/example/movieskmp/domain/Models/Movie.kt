package com.app.abstraction.domain
import com.base.abstractions.Domain.IEntity

class Movie : IEntity
{
    override var Id: Int = 0;
    var Name: String = ""
    var Overview: String = ""
    var PosterUrl: String = ""

    companion object
    {
        fun Create(name: String?, overview: String?, posterUrl: String?): Movie
        {
            requireNotNull(name) { "name must not be null" }
            requireNotNull(overview) { "overview must not be null" }

            val movie = Movie().apply {
                Name = name;
                Overview = overview;
                posterUrl?.let { PosterUrl = it }
            }

            return movie;
        }
    }
}