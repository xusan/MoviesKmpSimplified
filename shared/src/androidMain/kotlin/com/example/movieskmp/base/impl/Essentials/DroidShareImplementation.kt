package com.base.impl.Droid.Essentials

import android.content.Intent
import android.webkit.MimeTypeMap
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Essentials.IShare
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Essentials.Utils.FileSystemUtils
import com.base.impl.Droid.Utils.CurrentActivity
import java.io.File

internal class DroidShareImplementation : LoggableService(), IShare
{
    init
    {
        InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
    }

    override fun RequestShareFile(title: String, fullPath: String)
    {
        SpecificLogMethodStart(::RequestShareFile.name, title, fullPath)
        val fileUrl = FileSystemUtils.GetShareableFileUri(fullPath)
        val extension = File(fullPath).extension.lowercase()  // e.g. "jpg"
        val contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = contentType
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUrl)

            if (title.isNotEmpty()) {
                putExtra(Intent.EXTRA_TITLE, title)
            }
        }

        val chooser = Intent.createChooser(intent, title ?: "")
        chooser.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        CurrentActivity.Instance.startActivity(chooser)
    }
}