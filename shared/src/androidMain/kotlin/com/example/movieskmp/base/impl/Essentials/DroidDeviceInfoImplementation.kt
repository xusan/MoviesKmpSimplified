package com.base.impl.Droid.Essentials

import android.app.UiModeManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import com.base.abstractions.Common.VersionInfo
import com.base.abstractions.Essentials.Device.*
import com.base.impl.Droid.Utils.CurrentActivity
import kotlin.math.min

internal class DroidDeviceInfoImplementation : IDeviceInfo
{
    val tabletCrossover = 600

    override val Model: String
        get() = Build.MODEL

    override val Manufacturer: String
        get() = Build.MANUFACTURER

    override val Name: String
        get()
        {
            // DEVICE_NAME added in System.Global in API level 25
            // https://developer.android.com/reference/android/provider/Settings.Global#DEVICE_NAME
            var name = GetSystemSetting("device_name", true)
            if (name.isNullOrBlank())
                name = Model
            return name
        }

    override val VersionString: String
        get() = Build.VERSION.RELEASE

    override val Version: VersionInfo
        get() = VersionInfo.ParseVersion(VersionString)

    override val Platform: DevicePlatform
        get() = DevicePlatform.Android

    override val Idiom: DeviceIdiom
        get()
        {
            var currentIdiom = DeviceIdiom.Unknown

            // first try UIModeManager
            val uiModeManager = CurrentActivity.Instance.getSystemService(android.content.Context.UI_MODE_SERVICE) as? UiModeManager

            try
            {
                val uiMode = uiModeManager?.currentModeType ?: Configuration.UI_MODE_TYPE_UNDEFINED
                currentIdiom = DetectIdiom(uiMode)
            }
            catch (ex: Exception)
            {
                System.err.println("Unable to detect using UiModeManager: ${ex.message}")
            }

            // then try Configuration
            if (currentIdiom == DeviceIdiom.Unknown)
            {
                val configuration = CurrentActivity.Instance.resources?.configuration
                if (configuration != null)
                {
                    val minWidth = configuration.smallestScreenWidthDp
                    val isWide = minWidth >= tabletCrossover
                    currentIdiom = if (isWide) DeviceIdiom.Tablet else DeviceIdiom.Phone
                }
                else
                {
                    // start clutching at straws
                    val metrics = CurrentActivity.Instance.resources?.displayMetrics
                    if (metrics != null)
                    {
                        val minSize = min(metrics.widthPixels, metrics.heightPixels)
                        val isWide = minSize * metrics.density >= tabletCrossover
                        currentIdiom = if (isWide) DeviceIdiom.Tablet else DeviceIdiom.Phone
                    }
                }
            }

            // hope we got it somewhere
            return currentIdiom
        }

    override val DeviceType: DeviceTypeEnum
        get()
        {
            val isEmulator =
                (Build.BRAND.startsWith("generic", ignoreCase = false) && Build.DEVICE.startsWith("generic", ignoreCase = false)) ||
                        Build.FINGERPRINT.startsWith("generic", ignoreCase = false) ||
                        Build.FINGERPRINT.startsWith("unknown", ignoreCase = false) ||
                        Build.HARDWARE.contains("goldfish", ignoreCase = false) ||
                        Build.HARDWARE.contains("ranchu", ignoreCase = false) ||
                        Build.MODEL.contains("google_sdk", ignoreCase = false) ||
                        Build.MODEL.contains("Emulator", ignoreCase = false) ||
                        Build.MODEL.contains("Android SDK built for x86", ignoreCase = false) ||
                        Build.MANUFACTURER.contains("Genymotion", ignoreCase = false) ||
                        Build.MANUFACTURER.contains("VS Emulator", ignoreCase = false) ||
                        Build.PRODUCT.contains("emulator", ignoreCase = false) ||
                        Build.PRODUCT.contains("google_sdk", ignoreCase = false) ||
                        Build.PRODUCT.contains("sdk", ignoreCase = false) ||
                        Build.PRODUCT.contains("sdk_google", ignoreCase = false) ||
                        Build.PRODUCT.contains("sdk_x86", ignoreCase = false) ||
                        Build.PRODUCT.contains("simulator", ignoreCase = false) ||
                        Build.PRODUCT.contains("vbox86p", ignoreCase = false)

            if (isEmulator)
                return DeviceTypeEnum.Virtual

            return DeviceTypeEnum.Physical
        }

    fun DetectIdiom(uiMode: Int): DeviceIdiom
    {
        if (uiMode == Configuration.UI_MODE_TYPE_NORMAL)
            return DeviceIdiom.Unknown
        else if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION)
            return DeviceIdiom.TV
        else if (uiMode == Configuration.UI_MODE_TYPE_DESK)
            return DeviceIdiom.Desktop
        else if (uiMode == Configuration.UI_MODE_TYPE_WATCH)
            return DeviceIdiom.Watch

        return DeviceIdiom.Unknown
    }

    fun GetSystemSetting(name: String, isGlobal: Boolean = false): String?
    {
        if (isGlobal)
            return Settings.Global.getString(CurrentActivity.Instance.contentResolver, name)
        else
            return Settings.System.getString(CurrentActivity.Instance.contentResolver, name)
    }
}