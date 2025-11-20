package com.base.impl.Droid.Essentials

import android.content.Context
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Essentials.IDirectoryService
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Utils.CurrentActivity
import java.io.File
import java.lang.IllegalStateException

internal class DroidDirectoryService : LoggableService(), IDirectoryService
{
    private val context: Context get() = CurrentActivity.AppContext ?: throw IllegalStateException("CurrentActivity.AppContext is not set")

    override fun GetCacheDir(): String
    {
//        EnsureSpecificLoggerInit()
//        SpecificLogMethodStart(::GetCacheDir.name)
        val cacheDir = context.cacheDir.absolutePath;
        return cacheDir;
    }

    override fun GetAppDataDir(): String
    {
//        EnsureSpecificLoggerInit()
//        SpecificLogMethodStart(::GetAppDataDir.name)
        val appDir = context.filesDir.absolutePath;
        return appDir;
    }

    override fun IsExistDir(path: String): Boolean
    {
//        EnsureSpecificLoggerInit()
//        SpecificLogMethodStart(::IsExistDir.name, path)

        val dir = File(path)

        if (dir.exists() && dir.isDirectory) {
            return true
        } else {
            return false
        }
    }

    override fun CreateDir(path: String)
    {
//        EnsureSpecificLoggerInit()
//        SpecificLogMethodStart(::CreateDir.name)

        val dir = File(path)

        if (!dir.exists())
        {
            dir.mkdirs() // creates all necessary parent folders
        }
    }

    fun EnsureSpecificLoggerInit()
    {
        InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
    }
}