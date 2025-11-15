package com.base.abstractions.REST

interface IRestClient
{
    suspend fun DoHttpRequest(method: RestMethod, httpRequest: RestClientHttpRequest): String
}

class RestClientHttpRequest
{
    var RequestMethod: RestMethod = RestMethod.GET
    var Url: String = ""
    var JsonBody: String? = null
    var AccessToken: String? = null
    var RequestTimeout: TimeoutType = TimeoutType.Small
}

