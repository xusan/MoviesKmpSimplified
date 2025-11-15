package com.base.abstractions.Essentials

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/// The VersionTracking API provides an easy way to track an app's version on a device.
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IVersionTracking", exact = true)
interface IVersionTracking
{
    /// Starts tracking version information.
    fun Track()

    /// Gets a value indicating whether this is the first time this app has ever been launched on this device.
    val IsFirstLaunchEver: Boolean

    /// Gets a value indicating if this is the first launch of the app for the current version number.
    val IsFirstLaunchForCurrentVersion: Boolean

    /// Gets a value indicating if this is the first launch of the app for the current build number.
    val IsFirstLaunchForCurrentBuild: Boolean

    /// Gets the current version number of the app.
    val CurrentVersion: String

    /// Gets the current build of the app.
    val CurrentBuild: String

    /// Gets the version number for the previously run version.
    val PreviousVersion: String?

    /// Gets the build number for the previously run version.
    val PreviousBuild: String?

    /// Gets the version number of the first version of the app that was installed on this device.
    val FirstInstalledVersion: String?

    /// Gets the build number of first version of the app that was installed on this device.
    val FirstInstalledBuild: String?

    /// Gets the collection of version numbers of the app that ran on this device.
    val VersionHistory: List<String>

    /// Gets the collection of build numbers of the app that ran on this device.
    val BuildHistory: List<String>

    /// Determines if this is the first launch of the app for a specified version number.
    /// @param version The version number.
    /// @return true if this is the first launch of the app for the specified version number; otherwise false.
    fun IsFirstLaunchForVersion(version: String): Boolean

    /// Determines if this is the first launch of the app for a specified build number.
    /// @param build The build number.
    /// @return true if this is the first launch of the app for the specified build number; otherwise false.
    fun IsFirstLaunchForBuild(build: String): Boolean
}
