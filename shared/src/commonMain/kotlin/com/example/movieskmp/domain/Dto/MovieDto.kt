package com.app.abstraction.main.AppService.Dto

import com.base.abstractions.AppService.IAppDto

data class MovieDto
   (override var Id: Int,
    var Name: String,
    var Overview: String,
    var PosterUrl: String?): IAppDto