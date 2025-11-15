package com.base.impl.Droid.Essentials


import android.content.Intent
import android.net.Uri as AndroidUri
import androidx.browser.customtabs.CustomTabsIntent
import android.os.Build
import com.base.abstractions.Essentials.Browser.*
import com.base.impl.Droid.Essentials.Utils.PlatformUtils
import com.base.impl.Droid.Utils.*

internal class DroidBrowserImplementation : IBrowser
{
    override suspend fun OpenAsync(uri: String): Boolean
    {
        return this.OpenAsync(uri, BrowserLaunchOptions())
    }

    override suspend fun OpenAsync(uri: String, options: BrowserLaunchOptions): Boolean
    {
        val nativeUri = AndroidUri.parse(uri)

        when (options.LaunchMode)
        {
            BrowserLaunchMode.SystemPreferred ->
                LaunchChromeTabs(options, nativeUri)
            BrowserLaunchMode.External ->
                LaunchExternalBrowser(options, nativeUri)
        }

        return true
    }

    private fun LaunchChromeTabs(options: BrowserLaunchOptions, nativeUri: AndroidUri?)
    {
        val tabsBuilder = CustomTabsIntent.Builder()
        tabsBuilder.setShowTitle(true)

        val ptc = options.PreferredToolbarColor
        if (ptc != null)
            tabsBuilder.setToolbarColor(ptc.ToAndroid())

        if (options.TitleMode != BrowserTitleMode.Default)
            tabsBuilder.setShowTitle(options.TitleMode == BrowserTitleMode.Show)

        val tabsIntent = tabsBuilder.build()
        var tabsFlags: Int? = null

        var context = CurrentActivity.Instance

//        if (context == null)
//        {
//            context = Application.Context;
//
//            // If using ApplicationContext we need to set ClearTop/NewTask (See #225)
//            tabsFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            if (options.HasFlag(BrowserLaunchFlags.LaunchAdjacent))
            {
//                if (tabsFlags != null)
//                    tabsFlags = tabsFlags or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK
//                else
                    tabsFlags = Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        // Check if there's flags specified to use
        if (tabsFlags != null)
            tabsIntent.intent.setFlags(tabsFlags)

        if (nativeUri != null)
            tabsIntent.launchUrl(context, nativeUri)
    }

    private fun LaunchExternalBrowser(options: BrowserLaunchOptions, nativeUri: AndroidUri?)
    {
        val intent = Intent(Intent.ACTION_VIEW, nativeUri)
        var flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            if (options.HasFlag(BrowserLaunchFlags.LaunchAdjacent))
                flags = flags or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
        }

        intent.setFlags(flags)

        if (!PlatformUtils.IsIntentSupported(intent))
            throw UnsupportedOperationException("This feature is not supported on this Device.")

        CurrentActivity.Instance.startActivity(intent)
    }


}


