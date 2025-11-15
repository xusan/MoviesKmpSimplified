package com.base.abstractions.Essentials.Browser

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// <summary>
/// Additional flags that can be set to control how the browser opens.
/// </summary>
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "BrowserLaunchFlags", exact = true)
enum class BrowserLaunchFlags(val value: Int)
{
    /// <summary>No additional flags. This is the default.</summary>
    None(0),

    /// <summary>Only applicable to Android: launches a new activity adjacent to the current activity if available.</summary>
    LaunchAdjacent(1),

    /// <summary>Only applicable to iOS: launches the browser as a page sheet with the system preferred browser where supported.</summary>
    PresentAsPageSheet(2),

    /// <summary>Only applicable to iOS: launches the browser as a form sheet with the system preferred browser where supported.</summary>
    PresentAsFormSheet(4)
}