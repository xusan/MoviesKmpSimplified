package com.base.impl.REST

import com.base.abstractions.REST.IRestClient
import com.base.abstractions.REST.RestClientHttpRequest
import com.base.abstractions.REST.RestMethod
import com.base.abstractions.REST.TimeoutType
import com.base.impl.Diagnostic.LoggableService
import io.ktor.client.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

internal class RestClient : LoggableService(), IRestClient
{
    private val httpClient = HttpClient {
        // engine configuration (for all requests)
    }

    override suspend fun DoHttpRequest(method: RestMethod, httpRequest: RestClientHttpRequest): String
    {
        val timeoutMillis = when (httpRequest.RequestTimeout)
        {
            TimeoutType.Small -> 10_000L
            TimeoutType.Medium -> 30_000L
            TimeoutType.High -> 60_000L
            TimeoutType.VeryHigh -> 120_000L
        }

        val response: HttpResponse = httpClient.request(httpRequest.Url)
        {
            this.method = when (method)
            {
                RestMethod.GET -> HttpMethod.Get
                RestMethod.POST -> HttpMethod.Post
                RestMethod.PUT -> HttpMethod.Put
                RestMethod.DELETE -> HttpMethod.Delete
            }
            timeout()
            {
                requestTimeoutMillis = timeoutMillis
            }
            headers()
            {
                append(HttpHeaders.Accept, "application/json")
                if (!httpRequest.AccessToken.isNullOrEmpty())
                {
                    append(HttpHeaders.Authorization, "Bearer $httpRequest.AccessToken")
                }
//                httpRequest.HeaderValues?.forEach { (key, value) ->
//                    append(key, value)
//                }
            }
            if (!httpRequest.JsonBody.isNullOrEmpty())
            {
                contentType(ContentType.Application.Json)
                setBody(httpRequest.JsonBody)
            }
        }

        val responseContent = response.bodyAsText()
        return responseContent
    }



}