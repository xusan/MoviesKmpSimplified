package com.example.movieskmp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.movieskmp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private var isContentVisible = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGreeting.text = "Hello, Android!"

        binding.btnToggle.setOnClickListener {
            isContentVisible = !isContentVisible
            binding.contentLayout.visibility = if (isContentVisible) View.VISIBLE else View.GONE
        }
    }
}