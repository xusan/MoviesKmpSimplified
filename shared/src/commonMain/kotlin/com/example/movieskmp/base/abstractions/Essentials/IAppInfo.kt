package com.base.abstractions.Essentials

import com.base.abstractions.Common.VersionInfo
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName


/**
 * Represents information about the application.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IAppInfo", exact = true)
interface IAppInfo
{
    /**
     * Gets the application package name or identifier.
     *
     * On Android and iOS, this is the application package name. On Windows, this is the application GUID.
     */
    val PackageName: String

    /**
     * Gets the application name.
     */
    val Name: String

    /**
     * Gets the application version as a string representation.
     */
    val VersionString: String

    /**
     * Gets the application version as a [VersionInfo] object.
     */
    val Version: VersionInfo

    /**
     * Gets the application build number.
     */
    val BuildString: String

    /**
     * Open the settings menu or page for this application.
     */
    fun ShowSettingsUI()

    /**
     * Gets the requested layout direction of the system or application.
     */
    val RequestedLayoutDirection: LayoutDirection
}

/**
 * Enumerates possible layout directions.
 */
enum class LayoutDirection
{
    /**
     * The requested layout direction is unknown.
     */
    Unknown,

    /**
     * The requested layout direction is left-to-right.
     */
    LeftToRight,

    /**
     * The requested layout direction is right-to-left.
     */
    RightToLeft
}


