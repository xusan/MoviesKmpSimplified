package com.base.abstractions.Essentials.Browser

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// <summary>
/// Provides a way to display a web page inside an app.
/// </summary>
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IBrowser", exact = true)
interface IBrowser
{
    /// <summary>
    /// Open the browser to specified URI.
    /// </summary>
    /// <param name="uri">URI to open.</param>
    /// <returns>Completed task when browser is launched, but not necessarily closed. Result indicates if launching was successful or not.</returns>
    suspend fun OpenAsync(uri: String): Boolean

    /// <summary>
    /// Open the browser to specified URI.
    /// </summary>
    /// <param name="uri">URI to open.</param>
    /// <param name="options">Launch options for the browser.</param>
    /// <returns>Completed task when browser is launched, but not necessarily closed. Result indicates if launching was successful or not.</returns>
    suspend fun OpenAsync(uri: String, options: BrowserLaunchOptions): Boolean
}