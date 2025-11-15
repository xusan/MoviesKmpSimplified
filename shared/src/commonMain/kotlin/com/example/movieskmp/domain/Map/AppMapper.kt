package com.app.impl.cross.Map

import com.app.abstraction.domain.Movie
import com.app.abstraction.main.AppService.Dto.MovieDto
import com.base.abstractions.AppService.IAppDto
import com.base.abstractions.Domain.IEntity

internal inline fun <reified T: IAppDto> IEntity.ToDto() : T
{
    if(this is Movie)
    {
        if(T::class != MovieDto::class)
        {
            throw IllegalArgumentException("Movie entity can be converted only to MovieDto type but input T is ${T::class.simpleName}")
        }

        val dto = MovieDto(Id = this.Id,
            Name = this.Name,
            Overview = this.Overview,
            PosterUrl = this.PosterUrl)
        return dto as T;
    }
    throw IllegalArgumentException("toDto() failed: Can not find dto type for entity type ${this::class.simpleName} not found")
}

internal inline fun <reified T: IEntity> IAppDto.ToEntity() : T
{
    if(this is MovieDto)
    {
        if(T::class != Movie::class)
        {
            throw IllegalArgumentException("MovieDto can be converted only to Movie type but input T is ${T::class.simpleName}")
        }

        val entity = Movie.Create(this.Name, this.Overview, this.PosterUrl)
        entity.Id = this.Id;
        return entity as T;
    }

    throw IllegalArgumentException("ToEntity() failed: Can not find entity type for dto ${this::class.simpleName}")
}

