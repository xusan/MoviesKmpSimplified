package com.base.abstractions.AppService

class Some<T> private constructor(private val value: T?, val Exception: Throwable? = null)
{
    val Success: Boolean
        get()
        {
            return value != null;
        }

    val ValueOrThrow: T
        get()
        {
            return value ?: throw IllegalStateException("value is null");
        }

    companion object
    {
        fun <T> FromValue(value: T?): Some<T> = Some(value = value)
        fun <T> FromError(ex: Throwable): Some<T> = Some(value = null, Exception = ex)
    }
}