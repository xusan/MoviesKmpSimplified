package com.base.abstractions.REST

enum class RestMethod
{
    GET,
    POST,
    PUT,
    DELETE
}

enum class Priority(i: Int)
{
    HIGH(0),
    NORMAL(1),
    LOW(2),
    NONE(3),
}

enum class TimeoutType(val value: Int)
{
    Small(10),
    Medium(30),
    High(60),
    VeryHigh(120)
}

enum class HttpStatusCode(val value: Int)
{
    Unauthorized(401),
    ServiceUnavailable(502)
}