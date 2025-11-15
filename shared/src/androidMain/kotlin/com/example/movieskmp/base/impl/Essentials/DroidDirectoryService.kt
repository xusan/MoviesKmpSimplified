package com.base.impl.Droid.Essentials

import android.content.Context
import com.base.abstractions.Essentials.IDirectoryService
import com.base.impl.Droid.Utils.CurrentActivity
import java.io.File
import java.lang.IllegalStateException

internal class DroidDirectoryService : IDirectoryService
{
    private val context: Context get() = CurrentActivity.AppContext ?: throw IllegalStateException("CurrentActivity.AppContext is not set")

    override fun GetCacheDir(): String
    {
        val cacheDir = context.cacheDir.absolutePath;
        return cacheDir;
    }

    override fun GetAppDataDir(): String
    {
        val appDir = context.filesDir.absolutePath;
        return appDir;
    }

    override fun IsExistDir(path: String): Boolean
    {
        val dir = File(path)

        if (dir.exists() && dir.isDirectory) {
            return true
        } else {
            return false
        }
    }

    override fun CreateDir(path: String)
    {
        val dir = File(path)

        if (!dir.exists())
        {
            dir.mkdirs() // creates all necessary parent folders
        }
    }
}