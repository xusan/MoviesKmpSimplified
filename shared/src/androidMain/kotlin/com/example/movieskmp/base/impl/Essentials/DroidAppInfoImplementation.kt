package com.base.impl.Droid.Essentials


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.base.abstractions.Essentials.IAppInfo
import com.base.abstractions.Essentials.LayoutDirection
import com.base.impl.Droid.Utils.CurrentActivity

internal class DroidAppInfoImplementation : IAppInfo
{
    companion object
    {
        private val _name = lazy { CurrentActivity.Instance.applicationInfo.loadLabel(CurrentActivity.Instance.packageManager) as String }
        private val _packageName = lazy { CurrentActivity.Instance.packageName }
        // Deprecated in API 33: https://developer.android.com/reference/android/content/pm/PackageManager#getPackageInfo(java.lang.String,%20int)
        private val _packageInfo = lazy {
            val pm = CurrentActivity.Instance.packageManager
            val packageName = _packageName.value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            }
        }
    }

    override val PackageName: String
        get() = _packageName.value

    override val Name: String
        get() = _name.value

    override val Version: com.base.abstractions.Common.VersionInfo
        get() = com.base.abstractions.Common.VersionInfo.ParseVersion(VersionString)

    override val VersionString: String
        get() = _packageInfo.value.versionName ?: ""

    override val BuildString: String
        get() = PackageInfoCompat.getLongVersionCode(_packageInfo.value).toString()

    override fun ShowSettingsUI()
    {
        val context = CurrentActivity.Instance

        val settingsIntent = Intent()
        settingsIntent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
        settingsIntent.setData(Uri.parse("package:$PackageName"))

        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        settingsIntent.setFlags(flags)

        context.startActivity(settingsIntent)
    }

    override val RequestedLayoutDirection: LayoutDirection
        get() = GetLayoutDirection()

    private fun GetLayoutDirection(): LayoutDirection
    {
        val config = CurrentActivity.Instance.resources?.configuration
        if (config == null)
            return LayoutDirection.Unknown

        return if (config.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL)
            LayoutDirection.RightToLeft
        else
            LayoutDirection.LeftToRight
    }
}


