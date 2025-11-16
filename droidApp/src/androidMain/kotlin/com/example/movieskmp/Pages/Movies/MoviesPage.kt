package com.example.movieskmp.Pages.Movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.impl.Droid.Utils.ToVisibility
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.example.movieskmp.MainActivity
import com.example.movieskmp.Pages.Movies.Adapter.MoviesItems_Adapter
import com.example.movieskmp.databinding.PageMoviesBinding

class MoviesPage : DroidLifecyclePage()
{
    val viewModel: MoviesPageViewModel
        get() = ViewModel as MoviesPageViewModel

    private var _binding: PageMoviesBinding? = null
    val binding get() = _binding!!

    val adapter = MoviesItems_Adapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = PageMoviesBinding.inflate(inflater, container, false)

        val divider = DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL);
        val listlayout = LinearLayoutManager(this.context)

        binding.apply {

            recyclerView.layoutManager = listlayout
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(divider);

            btnMenu.setOnClickListener {
                (CurrentActivity.Instance as MainActivity).ShowSideSheet()
            }

            btnPlus.setOnClickListener {
                viewModel.AddCommand.Execute(null)
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.RefreshCommand.Execute(null)
            }
        }

        return binding.root
    }

    override fun OnViewModelPropertyChanged(propertyName: String)
    {
        super.OnViewModelPropertyChanged(propertyName)

        if(propertyName == viewModel::MovieItems.name)
        {
            adapter.OnCollectionSet();
        }
        else if(propertyName == viewModel::IsRefreshing.name)
        {
            binding.swipeRefreshLayout.isRefreshing = viewModel.IsRefreshing
        }
        else if(propertyName == viewModel::BusyLoading.name)
        {
            binding.progressBar.visibility = this.ViewModel.BusyLoading.ToVisibility();
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }
}