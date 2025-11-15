package com.base.abstractions.Essentials.Device

import com.base.abstractions.Common.VersionInfo
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName


/**
 * Represents information about the device.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IDeviceInfo", exact = true)
interface IDeviceInfo
{
    /**
     * Gets the model of the device.
     */
    val Model: String

    /**
     * Gets the manufacturer of the device.
     */
    val Manufacturer: String

    /**
     * Gets the name of the device.
     *
     * This value is often specified by the user of the device.
     */
    val Name: String

    /**
     * Gets the string representation of the version of the operating system.
     */
    val VersionString: String

    /**
     * Gets the version of the operating system.
     */
    val Version: VersionInfo

    /**
     * Gets the platform or operating system of the device.
     */
    val Platform: DevicePlatform

    /**
     * Gets the idiom (form factor) of the device.
     */
    val Idiom: DeviceIdiom

    /**
     * Gets the type of device the application is running on.
     */
    val DeviceType: DeviceTypeEnum
}


