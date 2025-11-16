package com.example.movieskmp.Pages.Movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import com.app.shared.ViewModels.AddEditMoviePageViewModel
import com.base.impl.Droid.Utils.ToVisibility
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.movieskmp.databinding.PageMovieAddEditBinding

class AddEditMoviePage : DroidLifecyclePage()
{
    val viewModel: AddEditMoviePageViewModel
        get() = ViewModel as AddEditMoviePageViewModel

    private var _binding: PageMovieAddEditBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = PageMovieAddEditBinding.inflate(inflater, container, false)

        binding.apply {

            txtTitle.setText(viewModel.Title);
            txtName.setText(viewModel.Model?.Name)
            txtDescription.setText(viewModel.Model?.Overview);
            btnDelete.visibility = viewModel.IsEdit.ToVisibility();

            OnPhotoChanged()

            txtName.addTextChangedListener {
                viewModel.Model?.Name = txtName.text.toString()
            }

            txtDescription.addTextChangedListener {
                viewModel.Model?.Overview = txtDescription.text.toString()
            }

            btnDelete.setOnClickListener {
                viewModel.DeleteCommand.Execute(null)
            }

            btnPhoto.setOnClickListener {
                viewModel.ChangePhotoCommand.Execute(null)
            }

            btnSave.setOnClickListener {
                viewModel.SaveCommand.Execute(null)
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.root.postDelayed({
            binding.btnDelete.isEnabled = true
        }, 600)
    }

    override fun OnViewModelPropertyChanged(propertyName: String)
    {
        super.OnViewModelPropertyChanged(propertyName)

        if(propertyName == AddEditMoviePageViewModel.PhotoChangedEvent)
        {
            OnPhotoChanged()
        }
    }

    private fun OnPhotoChanged()
    {
        if(viewModel.Model?.PosterUrl != null)
        {
            Glide.with(this)
                .load(viewModel.Model!!.PosterUrl)
                .override(200, 300)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgView)
        }
        else
        {
            binding.imgView.setImageDrawable(null)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }
}