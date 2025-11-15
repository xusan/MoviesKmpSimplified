package com.base.abstractions.Essentials.Device

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "DeviceIdiom", exact = true)
class DeviceIdiom private constructor(private val deviceIdiom: String)
{
    companion object
    {
        /**
         * Gets an instance of DeviceIdiom that represents a (mobile) phone idiom.
         */
        val Phone: DeviceIdiom = DeviceIdiom("Phone")

        /**
         * Gets an instance of DeviceIdiom that represents a tablet idiom.
         */
        val Tablet: DeviceIdiom = DeviceIdiom("Tablet")

        /**
         * Gets an instance of DeviceIdiom that represents a desktop computer idiom.
         */
        val Desktop: DeviceIdiom = DeviceIdiom("Desktop")

        /**
         * Gets an instance of DeviceIdiom that represents a television (TV) idiom.
         */
        val TV: DeviceIdiom = DeviceIdiom("TV")

        /**
         * Gets an instance of DeviceIdiom that represents a watch idiom.
         */
        val Watch: DeviceIdiom = DeviceIdiom("Watch")

        /**
         * Gets an instance of DeviceIdiom that represents an unknown idiom. This is used for when the current device idiom is unknown.
         */
        val Unknown: DeviceIdiom = DeviceIdiom("Unknown")

        /**
         * Creates a new device idiom instance. This can be used to define your custom idioms.
         * @param deviceIdiom The idiom name of the device.
         * @return A new instance of DeviceIdiom with the specified idiom type.
         */
        fun Create(deviceIdiom: String): DeviceIdiom =
            DeviceIdiom(deviceIdiom)
    }

    init
    {
        if (deviceIdiom.isEmpty())
            throw IllegalArgumentException("deviceIdiom")
    }

    /**
     * Compares the underlying DeviceIdiom instances.
     * @param other DeviceIdiom object to compare with.
     * @return true if they are equal, otherwise false.
     */
    fun Equals(other: DeviceIdiom): Boolean =
        Equals(other.deviceIdiom)

    internal fun Equals(other: String): Boolean =
        deviceIdiom == other

    /**
     * Compares the underlying DeviceIdiom instances.
     */
    override fun equals(other: Any?): Boolean =
        other is DeviceIdiom && Equals(other)

    /**
     * Gets the hash code for this idiom instance.
     * @return The computed hash code for this device idiom or 0 when the device idiom is null.
     */
    override fun hashCode(): Int =
        deviceIdiom.hashCode()

    /**
     * Returns a string representation of the current device idiom.
     * @return A string representation of this instance in the format of {device idiom} or an empty string when no device idiom is set.
     */
    override fun toString(): String =
        deviceIdiom
}