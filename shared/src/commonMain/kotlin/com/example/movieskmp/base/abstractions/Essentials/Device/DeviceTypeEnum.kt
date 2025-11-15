package com.base.abstractions.Essentials.Device

/**
 * Represents the type of device.
 */

enum class DeviceTypeEnum(val value: Int)
{
    /**
     * An unknown device type.
     */
    Unknown(0),

    /**
     * The device is a physical device, such as an iPhone, Android tablet, or Windows/macOS desktop.
     */
    Physical(1),

    /**
     * The device is virtual, such as the iOS Simulator, Android emulators, or Windows emulators.
     */
    Virtual(2)
}