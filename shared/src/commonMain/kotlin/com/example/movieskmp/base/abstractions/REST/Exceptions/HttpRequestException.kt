package com.base.abstractions.REST.Exceptions

class HttpRequestException(val statusCode: Int?,
                           override val message: String,
                           val causeException: Throwable? = null
) : RuntimeException(message, causeException)