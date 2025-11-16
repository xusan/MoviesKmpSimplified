package com.example.movieskmp.Pages.Movies.Adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.shared.ViewModels.ItemViewModel.MovieItemViewModel
import com.base.mvvm.Helpers.ObservableCollection
import com.base.mvvm.Helpers.ObservableCollection.Change
import com.example.movieskmp.Pages.Movies.MoviesPage
import com.example.movieskmp.R

class MoviesItems_Adapter(private val page: MoviesPage) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var collection: ObservableCollection<MovieItemViewModel>? = null

    fun OnCollectionSet()
    {
        if (this.page.viewModel.MovieItems != null)
        {
            if (collection != null)
            {
                collection?.CollectionChanged -= ::Collection_CollectionChanged
            }

            collection = this.page.viewModel.MovieItems
            collection?.CollectionChanged += ::Collection_CollectionChanged
        }

        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = collection?.Count() ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val cellView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cell_movie, parent, false)

        val viewHolder = MovieItem_ViewHolder(cellView)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val model = this.collection!![position]
        val viewHolder = holder as MovieItem_ViewHolder
        viewHolder.SetData(model)
    }

    private fun Collection_CollectionChanged(arg: Change)
    {
        when (arg) {
            is Change.Added ->
            {
                this.notifyItemInserted(arg.index)
                if(arg.index == 0)
                {
                    this.page.binding.recyclerView.scrollToPosition(0)
                }
            }
            is Change.Removed ->
            {
                this.notifyItemRemoved(arg.index)
            }
            is Change.Replaced ->
            {
                this.notifyItemChanged(arg.index)
            }
            is Change.Cleared ->
            {
                this.notifyDataSetChanged()
            }
        }
    }
}

