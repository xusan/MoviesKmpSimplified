package com.example.movieskmp.Pages.Movies.Adapter


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.impl.Droid.Utils.CurrentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.movieskmp.MainActivity
import com.example.movieskmp.R


class MovieItem_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private var imgView: ImageView
    private var txtName: TextView
    private var txtDescription: TextView

    init
    {
        imgView = itemView.findViewById<ImageView>(R.id.imgView)
        txtName = itemView.findViewById<TextView>(R.id.txtName)
        txtDescription = itemView.findViewById<TextView>(R.id.txtDescription)
    }

    fun SetData(model: MovieItemViewModel)
    {
        this.txtName.text = model.Name
        this.txtDescription.text = model.Overview

        if (!model.PosterUrl.isNullOrEmpty())
        {
            Glide.with(this.itemView.context)
                .load(model.PosterUrl)
                .override(200, 300)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgView)
        }
        else
        {
            imgView.setImageDrawable(null)
        }

        this.itemView.tag = model
        if (!this.itemView.hasOnClickListeners())
        {
            this.itemView.setOnClickListener { view -> ItemView_Click(view) }
        }
    }

    private fun ItemView_Click(sender: Any?)
    {
        val itemView = sender as ViewGroup
        val clickItem = itemView.tag as MovieItemViewModel
        val pageVm = (CurrentActivity.Instance as MainActivity).GetCurrentViewModel() as MoviesPageViewModel
        pageVm.ItemTappedCommand.Execute(clickItem)
    }
}


