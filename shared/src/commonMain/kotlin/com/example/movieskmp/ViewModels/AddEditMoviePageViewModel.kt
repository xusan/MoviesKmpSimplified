package com.app.shared.ViewModels

import com.app.abstraction.domain.Movie
import com.app.abstraction.main.AppService.Dto.MovieDto
import com.app.shared.Base.AppPageViewModel
import com.app.shared.Base.PageInjectedServices
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.base.abstractions.AppService.Some
import com.base.abstractions.Essentials.IMediaPickerService
import com.base.abstractions.Essentials.MediaOptions
import com.base.mvvm.Helpers.AsyncCommand
import com.base.mvvm.Helpers.CommonStrings
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.NavigationParameters
import com.example.movieskmp.domain.AppServices.IMovieService
import io.ktor.client.utils.unwrapCancellationException
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "AddEditMoviePageViewModel", exact = true)//We need it to generate an exact name like AddEditMoviePageViewModel. By default, it will generate SACAddEditMoviePageViewModel and this can cause issue for navigation as the page is registered for the "AddEditMoviePageViewModel" key
class AddEditMoviePageViewModel(injectedService: PageInjectedServices) : MovieDetailPageViewModel(injectedService)
{
    companion object
    {
        const val NEW_ITEM: String = "newItem";
        const val UPDATE_ITEM: String = "updateItem";
        const val REMOVE_ITEM: String = "removeItem";
        const val PhotoChangedEvent: String = "PhotoChanged";
    }

    //private val movieService: IMovieService by inject()
    private val mediaPickerService: IMediaPickerService by inject()

    var SaveCommand: AsyncCommand;
    var ChangePhotoCommand: AsyncCommand;
    var DeleteCommand: AsyncCommand;
//    var Model: MovieItemViewModel? = null
//        set(value)
//        {
//            SetProperty(::Model.name, field, value) { field = it }
//        }
    var IsEdit: Boolean = false
        set(value)
        {
            SetProperty(::IsEdit.name, field, value) { field = it }
        }

    init
    {
        SaveCommand = AsyncCommand(MainThreadScope,::OnSaveCommand)
        ChangePhotoCommand = AsyncCommand(MainThreadScope,::OnChangePhotoCommand)
        DeleteCommand = AsyncCommand(MainThreadScope,::OnDeleteCommand)
    }

    override fun Initialize(parameters: INavigationParameters)
    {
        LogMethodStart(::Initialize.name)
        super.Initialize(parameters)

        if (parameters.ContainsKey(MoviesPageViewModel.SELECTED_ITEM))
        {
            this.IsEdit = true;
            this.Title = "Edit";
            val movieId = parameters.GetValue<Int>(MoviesPageViewModel.SELECTED_ITEM)!!;
            MainThreadScope.launch { LoadMovie(movieId) }
        }
        else
        {
            this.Model = MovieItemViewModel()
            this.Title = "Add new"
        }
    }

    override fun OnNavigatedTo(parameters: INavigationParameters)
    {
        //keep empty to override base one
    }

    suspend fun OnChangePhotoCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnChangePhotoCommand.name)
            val deleteText = if(!this.Model?.PosterUrl.isNullOrEmpty()) "Delete" else null;

            val buttons = arrayOf("Pick Photo", "Take Photo");
            val actionResult = Services.AlertDialogService.DisplayActionSheet("Set photo from",cancel = "Cancel", destruction = deleteText, *buttons);

            if (actionResult == buttons[0])
            {
                val photo = mediaPickerService.GetPhotoAsync(MediaOptions());
                if(photo != null)
                {
                    this.Model?.PosterUrl = photo.FilePath;
                }
                else
                {
                    Services.LoggingService.LogWarning("AddEditMoviePageViewModel: GetPhotoAsync() returned null")
                }
            }
            else if (actionResult == buttons[1])
            {
                val photo = mediaPickerService.TakePhotoAsync(MediaOptions());
                if(photo != null)
                {
                    this.Model?.PosterUrl = photo.FilePath;
                }
                else
                {
                    Services.LoggingService.LogWarning("AddEditMoviePageViewModel: TakePhotoAsync() returned null")
                }
            }
            else if (actionResult == deleteText)
            {
                this.Model?.PosterUrl = null;
            }

            RaisePropertyChanged(PhotoChangedEvent)
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex);
        }
    }

    suspend fun OnSaveCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnSaveCommand.name)

            if (this.Model?.Name.isNullOrEmpty())
            {
                Services.SnackBarService.ShowError("The Name field is required");
                return;
            }
            else if (this.Model?.Overview.isNullOrEmpty())
            {
                Services.SnackBarService.ShowError("The Overview field is required");
                return;
            }

            var success = false
            var movieId = 0
            var error: Throwable? = null
            if (this.IsEdit)
            {
                //TODO use mapper
                val dtoModel = Model?.ToDto()!!
                val result = movieService.UpdateAsync(dtoModel);
                success = result.Success
                movieId = Model!!.Id
                error = result.Exception
            }
            else
            {
                val result = movieService.AddAsync(this.Model?.Name!!, this.Model?.Overview!!, this.Model?.PosterUrl);
                success = result.Success
                movieId = result.ValueOrThrow.Id
                error = result.Exception
            }

            if (success)
            {
                val key = if(this.IsEdit) UPDATE_ITEM else NEW_ITEM;
                NavigateBack(NavigationParameters()
                {
                    add(key, movieId)
                });
            }
            else
            {
                error?.let {
                    HandleUIError(it)
                }
            }

        }
        catch (ex: Throwable)
        {
            HandleUIError(ex);
        }
    }

    suspend fun OnDeleteCommand(arg: Any?)
    {
        try
        {
            LogMethodStart(::OnDeleteCommand.name)

            val res = Services.AlertDialogService.ConfirmAlert("Confirm", "Are you sure you want to delete this item?", "Yes", "No");

            if (res == true)
            {
                val result = movieService.RemoveAsync(this.Model!!.Id);

                if (result.Success)
                {
                    NavigateToRoot(NavigationParameters()
                    {
                        add(REMOVE_ITEM, Model!!.Id)
                    });
                }
                else
                {
                    result.Exception?.let { HandleUIError(it) }
                }
            }
        }
        catch (ex: Throwable)
        {
            HandleUIError(ex);
        }
    }
}