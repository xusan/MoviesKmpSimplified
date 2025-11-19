package com.base.impl.Droid.Essentials


import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Essentials.IPreferences
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Utils.CurrentActivity
import java.util.Date
import java.util.Locale
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

internal class DroidPreferencesImplementation : LoggableService(), IPreferences
{
    private val locker = Any()

//    init
//    {
//        InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
//    }

    override fun ContainsKey(key: String, sharedName: String?): Boolean
    {
        synchronized(locker)
        {
            //SpecificLogMethodStart(::ContainsKey.name, key, sharedName)
            val sharedPreferences = GetSharedPreferences(sharedName)
            return sharedPreferences.contains(key)
        }
    }

    override fun Remove(key: String, sharedName: String?)
    {
        synchronized(locker)
        {
            //SpecificLogMethodStart(::Remove.name, key, sharedName)
            val sharedPreferences = GetSharedPreferences(sharedName)
            val editor = sharedPreferences.edit()
            editor.remove(key).apply()
        }
    }

    override fun Clear(sharedName: String?)
    {
        synchronized(locker)
        {
            //SpecificLogMethodStart(::Clear.name, sharedName)
            val sharedPreferences = GetSharedPreferences(sharedName)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
        }
    }

    override fun <T> Set(key: String, value: T, sharedName: String?)
    {
        synchronized(locker)
        {
            //SpecificLogMethodStart("Set", key, sharedName)
            val sharedPreferences = GetSharedPreferences(sharedName)
            val editor = sharedPreferences.edit()
            if (value == null)
            {
                editor.remove(key)
            }
            else
            {
                when (value)
                {
                    is String -> {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        editor.putString(key, value)
                    }
                    is Int -> {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        editor.putInt(key, value)
                    }
                    is Boolean -> {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        editor.putBoolean(key, value)
                    }
                    is Long -> {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        editor.putLong(key, value)
                    }
                    is Double ->
                    {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        val valueString = String.format(Locale.ROOT, "%s", value)
                        editor.putString(key, valueString)
                    }
                    is Float -> {
                        //SpecificLogMethodStart("Set", key, value, sharedName)
                        editor.putFloat(key, value)
                    }
                    is Date -> {
                        //SpecificLogMethodStart("Set", key, value.time, sharedName)
                        editor.putLong(key, value.time)
                    }
//                    is OffsetDateTime -> {
//                        editor.putString(key, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
//                    }
                }
            }
            editor.apply()
        }
    }

    override fun <T> Get(key: String, defaultValue: T, sharedName: String?): T
    {
        synchronized(locker)
        {
            //SpecificLogMethodStart("Get", key, sharedName)

            var value: Any? = null
            val sharedPreferences = GetSharedPreferences(sharedName)
            if (defaultValue == null)
            {
                value = sharedPreferences.getString(key, null)
            }
            else
            {
                when (defaultValue)
                {
                    is Int ->
                        value = sharedPreferences.getInt(key, defaultValue)
                    is Boolean ->
                        value = sharedPreferences.getBoolean(key, defaultValue)
                    is Long ->
                        value = sharedPreferences.getLong(key, defaultValue)
                    is Double ->
                    {
                        val savedDouble = sharedPreferences.getString(key, null)
                        if (savedDouble.isNullOrBlank())
                        {
                            value = defaultValue
                        }
                        else
                        {
                            var outDouble = savedDouble.toDoubleOrNull()
                            if (outDouble == null)
                            {
                                val maxString = String.format(Locale.ROOT, "%s", Double.MAX_VALUE)
                                outDouble = if (savedDouble == maxString) Double.MAX_VALUE else Double.MIN_VALUE
                            }

                            value = outDouble
                        }
                    }
                    is Float ->
                        value = sharedPreferences.getFloat(key, defaultValue)
                    is String ->
                    {
                        // the case when the string is not null
                        value = sharedPreferences.getString(key, defaultValue)
                    }
                    is Date ->
                    {
                        val encodedValue = sharedPreferences.getLong(key, defaultValue.time)
                        value = Date(encodedValue)
                    }
//                    is OffsetDateTime ->
//                    {
//                        val savedDateTimeOffset = sharedPreferences.getString(key, defaultValue.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
//                        try
//                        {
//                            value = OffsetDateTime.parse(savedDateTimeOffset, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
//                        }
//                        catch (e: Exception)
//                        {
//                            value = defaultValue
//                        }
//                    }
                }
            }

            //specificLogger.Log("DroidPreferencesImplementation.Get($key) -> $value")
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
    }

    companion object
    {
        internal fun GetSharedPreferences(sharedName: String?): SharedPreferences
        {
            val context = CurrentActivity.AppContext

            @Suppress("DEPRECATION")
            return if (sharedName.isNullOrBlank())
                PreferenceManager.getDefaultSharedPreferences(context)
            else
                context!!.getSharedPreferences(sharedName, Context.MODE_PRIVATE)
        }
    }
}


