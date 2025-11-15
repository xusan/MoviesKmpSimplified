package com.base.abstractions.Essentials.Device

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents the device platform that the application is running on.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "DevicePlatform", exact = true)
class DevicePlatform private constructor(private val devicePlatform: String)
{
    companion object
    {
        /**
         * Gets an instance of DevicePlatform that represents Android.
         */
        val Android: DevicePlatform = DevicePlatform("Android")

        /**
         * Gets an instance of DevicePlatform that represents iOS.
         */
        val iOS: DevicePlatform = DevicePlatform("iOS")

        /**
         * Gets an instance of DevicePlatform that represents macOS.
         * Note, this is different than MacCatalyst.
         */
        val macOS: DevicePlatform = DevicePlatform("macOS")

        /**
         * Gets an instance of DevicePlatform that represents Mac Catalyst.
         * Note, this is different than macOS.
         */
        val MacCatalyst: DevicePlatform = DevicePlatform("MacCatalyst")

        /**
         * Gets an instance of DevicePlatform that represents Apple tvOS.
         */
        val tvOS: DevicePlatform = DevicePlatform("tvOS")

        /**
         * Gets an instance of DevicePlatform that represents Samsung Tizen.
         */
        val Tizen: DevicePlatform = DevicePlatform("Tizen")

        /**
         * Gets an instance of DevicePlatform that represents UWP.
         */
        @Deprecated("Use WinUI instead.")
        val UWP: DevicePlatform = DevicePlatform("WinUI")

        /**
         * Gets an instance of DevicePlatform that represents WinUI.
         */
        val WinUI: DevicePlatform = DevicePlatform("WinUI")

        /**
         * Gets an instance of DevicePlatform that represents Apple watchOS.
         */
        val watchOS: DevicePlatform = DevicePlatform("watchOS")

        /**
         * Gets an instance of DevicePlatform that represents an unknown platform. This is used for when the current platform is unknown.
         */
        val Unknown: DevicePlatform = DevicePlatform("Unknown")

        /**
         * Creates a new device platform instance. This can be used to define your custom platforms.
         * @param devicePlatform The device platform identifier.
         * @return A new instance of DevicePlatform with the specified platform identifier.
         */
        fun Create(devicePlatform: String): DevicePlatform =
            DevicePlatform(devicePlatform)
    }

    init
    {
        if (devicePlatform.isEmpty())
            throw IllegalArgumentException("devicePlatform")
    }

    /**
     * Compares the underlying DevicePlatform instances.
     * @param other DevicePlatform object to compare with.
     * @return true if they are equal, otherwise false.
     */
    fun Equals(other: DevicePlatform): Boolean =
        Equals(other.devicePlatform)

    internal fun Equals(other: String): Boolean =
        devicePlatform == other

    override fun equals(other: Any?): Boolean =
        other is DevicePlatform && Equals(other)

    /**
     * Gets the hash code for this platform instance.
     * @return The computed hash code for this device platform or 0 when the device platform is null.
     */
    override fun hashCode(): Int =
        devicePlatform.hashCode()

    /**
     * Returns a string representation of the current value of the device platform.
     * @return A string representation of this instance in the format of {device platform} or an empty string when no device platform is set.
     */
    override fun toString(): String =
        devicePlatform
}

/**
 * Equality operator for equals.
 * @param left Left to compare.
 * @param right Right to compare.
 * @return true if objects are equal, otherwise false.
 */
operator fun DevicePlatform.compareTo(right: DevicePlatform): Int =
    if (this.Equals(right)) 0 else 1