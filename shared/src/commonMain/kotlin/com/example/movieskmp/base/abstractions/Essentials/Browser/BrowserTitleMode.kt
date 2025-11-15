package com.base.abstractions.Essentials.Browser

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// <summary>
/// Mode for the in-app browser title.
/// </summary>
/// <remarks>These values only apply to Android.</remarks>
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "BrowserTitleMode", exact = true)
enum class BrowserTitleMode
{
    /// <summary>Uses the system default.</summary>
    Default,

    /// <summary>Show the title.</summary>
    Show,

    /// <summary>Hide the title.</summary>
    Hide
}