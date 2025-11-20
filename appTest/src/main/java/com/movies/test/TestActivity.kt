package com.movies.test

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity()
{
    companion object {
        var RegisterDI: ((AppCompatActivity) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        RegisterDI?.invoke(this)
    }
}