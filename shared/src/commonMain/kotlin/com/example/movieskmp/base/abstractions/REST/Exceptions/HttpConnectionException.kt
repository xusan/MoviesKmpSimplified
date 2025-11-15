package com.base.abstractions.REST.Exceptions

class HttpConnectionException(override val message: String,
                              val causeException: Throwable? = null
) : Exception(message, causeException)