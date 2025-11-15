package com.base.abstractions.Essentials.Browser

import com.base.abstractions.Common.XfColor
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// <summary>
/// Optional setting to open the browser with.
/// </summary>
/// <remarks>Not all settings apply to all operating systems. Check documentation for more information.</remarks>
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "BrowserLaunchOptions", exact = true)
class BrowserLaunchOptions
{
    /// <summary>
    /// Gets or sets the preferred color of the toolbar background of the in-app browser.
    /// </summary>
    /// <remarks>This setting only applies to iOS and Android.</remarks>
    var PreferredToolbarColor: XfColor? = null

    /// <summary>
    /// Gets or sets the preferred color of the controls on the in-app browser.
    /// </summary>
    /// <remarks>This setting only applies to iOS.</remarks>
    var PreferredControlColor: XfColor? = null

    /// <summary>
    /// Gets or sets how the browser should be launched.
    /// </summary>
    /// <remarks>The default value is <see cref="BrowserLaunchMode.SystemPreferred"/>.</remarks>
    var LaunchMode: BrowserLaunchMode = BrowserLaunchMode.SystemPreferred

    /// <summary>
    /// Gets or sets the preferred mode for the title display.
    /// </summary>
    /// <remarks>The default value is <see cref="BrowserTitleMode.Default"/>. This setting only applies to Android.</remarks>
    var TitleMode: BrowserTitleMode = BrowserTitleMode.Default

    /// <summary>
    /// Gets or sets additional launch flags that may or may not take effect based on the device and <see cref="LaunchMode"/>.
    /// </summary>
    /// <remarks>The default value is <see cref="BrowserLaunchFlags.None"/>. Not all flags work on all platforms, check the flag descriptions.</remarks>
    var Flags: BrowserLaunchFlags = BrowserLaunchFlags.None

    fun HasFlag(flag: BrowserLaunchFlags): Boolean = (Flags.value and flag.value) != 0
}