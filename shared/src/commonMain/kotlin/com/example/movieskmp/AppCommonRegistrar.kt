package com.example.movieskmp


import com.app.abstraction.domain.Movie
import com.app.impl.cross.Infasructures.MyInfrastructureService
import com.app.impl.cross.Infasructures.Repository.DbInitilizer
import com.app.impl.cross.Infasructures.Repository.MovieRepository
import com.app.impl.cross.Infasructures.Repository.Tables.MovieTb
import com.app.impl.cross.Map.RepoMovieMapper
import com.base.abstractions.IInfrastructureServices
import com.base.abstractions.Repository.ILocalDbInitilizer
import com.base.abstractions.Repository.IRepoMapper
import com.base.abstractions.Repository.IRepository
import com.example.movieskmp.domain.AppServices.IMovieService
import com.example.movieskmp.domain.AppServices.MoviesService
import com.example.movieskmp.domain.Infasructures.REST.IMovieRestService
import com.example.movieskmp.domain.Infasructures.REST.MovieRestService
import org.koin.core.module.Module
import org.koin.dsl.module

class AppCommonRegistrar
{
    companion object Companion
    {
        fun RegisterTypes() : List<Module> = listOf(

            RegisterInfrastructureService(),
            RegisterAppService()
        )

        fun RegisterInfrastructureService(skipBase: Boolean = false) : Module =  module()
        {
            //infrastructures
            //Repository
            single<ILocalDbInitilizer> { DbInitilizer() }
            single<IRepoMapper<Movie, MovieTb>> { RepoMovieMapper() }
            single<IRepository<Movie>> { MovieRepository() }
            //REST
            single<IMovieRestService> { MovieRestService() }
            //common
            single<IInfrastructureServices> { MyInfrastructureService() }
//            //register and call init for MyErrorTrackingService
//            single<IErrorTrackingService>(createdAtStart = true)
//            {
//                MyErrorTrackingService().apply {Initilize()}
//            }
        }

        fun RegisterAppService(): Module = module()
        {
            //app services
            single<IMovieService> { MoviesService() }
        }
    }

}