package com.base.abstractions.Essentials

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The Preferences API helps to store application preferences in a key/value store.
 *
 * Each platform uses the platform-provided APIs for storing application/user preferences:
 * - iOS: NSUserDefaults
 * - Android: SharedPreferences
 * - Windows: ApplicationDataContainer
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IPreferences", exact = true)
interface IPreferences
{
    /**
     * Checks for the existence of a given key.
     *
     * @param key The key to check.
     * @param sharedName Shared container name.
     * @return `true` if the key exists in the preferences, otherwise `false`.
     */
    fun ContainsKey(key: String, sharedName: String? = null): Boolean

    /**
     * Removes a key and its associated value if it exists.
     *
     * @param key The key to remove.
     * @param sharedName Shared container name.
     */
    fun Remove(key: String, sharedName: String? = null)

    /**
     * Clears all keys and values.
     *
     * @param sharedName Shared container name.
     */
    fun Clear(sharedName: String? = null)

    /**
     * Sets a value for a given key.
     *
     * @param T Type of the object that is stored in this preference.
     * @param key The key to set the value for.
     * @param value Value to set.
     * @param sharedName Shared container name.
     */
    fun <T> Set(key: String, value: T, sharedName: String? = null)

    /**
     * Gets the value for a given key, or the default specified if the key does not exist.
     *
     * @param T The type of the object stored for this preference.
     * @param key The key to retrieve the value for.
     * @param defaultValue The default value to return when no existing value for [key] exists.
     * @param sharedName Shared container name.
     * @return Value for the given key, or the value in [defaultValue] if it does not exist.
     */
    fun <T> Get(key: String, defaultValue: T, sharedName: String? = null): T
}