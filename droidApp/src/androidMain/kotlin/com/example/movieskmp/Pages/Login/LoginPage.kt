package com.example.movieskmp.Pages.Login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.app.shared.ViewModels.LoginPageViewModel
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.example.movieskmp.databinding.PageLoginBinding

class LoginPage() : DroidLifecyclePage()
{
    val viewModel: LoginPageViewModel
        get() = ViewModel as LoginPageViewModel

    private var _binding: PageLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = PageLoginBinding.inflate(inflater, container, false)

        binding.apply {

            txtLogin.addTextChangedListener {
                viewModel.Login = txtLogin.text.toString()
            }

            txtPassword.addTextChangedListener {
                viewModel.Password = txtPassword.text.toString()
            }

            btnSubmit.setOnClickListener {
                viewModel.SubmitCommand.Execute(null)
            }
        }

        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }
}