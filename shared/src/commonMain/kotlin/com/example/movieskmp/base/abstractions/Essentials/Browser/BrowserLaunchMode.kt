package com.base.abstractions.Essentials.Browser

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// <summary>
/// Launch type of the browser.
/// </summary>
/// <remarks>It's recommended to use the <see cref="BrowserLaunchMode.SystemPreferred"/> as it is the default and gracefully falls back if needed.</remarks>
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "BrowserLaunchMode", exact = true)
enum class BrowserLaunchMode
{
    /// <summary>Launch the optimized system browser and stay inside of your application. Chrome Custom Tabs on Android and SFSafariViewController on iOS.</summary>
    SystemPreferred,

    /// <summary>Use the default external launcher to open the browser outside of the app.</summary>
    External
}