package com.app.shared.ViewModels.ItemViewModel

import com.app.abstraction.main.AppService.Dto.MovieDto
import com.base.mvvm.ViewModels.BindableBase


class MovieItemViewModel() : BindableBase()
{
    private lateinit var movieDto: MovieDto;
    constructor(dto: MovieDto) : this()
    {
        Id = dto.Id
        Name = dto.Name
        Overview = dto.Overview
        PosterUrl = dto.PosterUrl
        movieDto = dto
    }
    var Id: Int = 0
        set(value)
        {
            SetProperty(::Id.name, field, value) { field = it }
        }
    var Name: String = ""
        set(value)
        {
            SetProperty(::Name.name, field, value) { field = it }
        }
    var Overview: String = ""
        set(value)
        {
            SetProperty(::Overview.name, field, value) { field = it }
        }
    var PosterUrl: String? = ""
        set(value)
        {
            SetProperty(::PosterUrl.name, field, value) { field = it }
        }

    fun ToDto(): MovieDto
    {
        movieDto.Id = Id
        movieDto.Name = Name
        movieDto.Overview = Overview
        movieDto.PosterUrl = PosterUrl

        return movieDto
    }
}