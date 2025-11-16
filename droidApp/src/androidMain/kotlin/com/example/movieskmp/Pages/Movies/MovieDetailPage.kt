package com.example.movieskmp.Pages.Movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shared.ViewModels.MovieDetailPageViewModel
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.impl.Droid.Utils.ToVisibility
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.movieskmp.MainActivity
import com.example.movieskmp.databinding.PageMovieDetailBinding

class MovieDetailPage : DroidLifecyclePage()
{
    val viewModel: MovieDetailPageViewModel
        get() = ViewModel as MovieDetailPageViewModel

    private var _binding: PageMovieDetailBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = PageMovieDetailBinding.inflate(inflater, container, false)

        binding.apply {

            OnModelUpdated()

            btnEdit.setOnClickListener {
                viewModel.EditCommand.Execute(null)
            }
        }

        return binding.root
    }

    override fun OnViewModelPropertyChanged(propertyName: String)
    {
        super.OnViewModelPropertyChanged(propertyName)

        if(propertyName == viewModel::Model.name)
        {
            OnModelUpdated()
        }
    }

    fun OnModelUpdated()
    {
        binding.apply {
            txtName.text = viewModel.Model?.Name
            txtDescription.text = viewModel.Model?.Overview

            if(viewModel.Model?.PosterUrl != null)
            {
                Glide.with(this@MovieDetailPage)
                    .load(viewModel.Model!!.PosterUrl)
                    .override(200, 300)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgView)
            }
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }
}