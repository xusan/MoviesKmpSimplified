package com.base.impl.REST

import com.base.abstractions.IConstant
import com.base.abstractions.Messaging.GetEvent
import com.base.abstractions.Messaging.IMessagesCenter
import com.base.abstractions.REST.AuthErrorEvent
import com.base.abstractions.REST.Exceptions.AuthExpiredException
import com.base.abstractions.REST.IAuthTokenService
import com.base.abstractions.REST.IRestClient
import com.base.impl.Diagnostic.LoggableService
import io.ktor.client.plugins.ClientRequestException
import org.koin.core.component.inject
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.client.network.sockets.*
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.*
import com.base.abstractions.REST.*
import com.base.abstractions.REST.Exceptions.HttpConnectionException
import com.base.abstractions.REST.Exceptions.HttpRequestException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.base.abstractions.REST.Exceptions.ServerApiException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject


open class RestService : LoggableService()
{
    val authTokenService: IAuthTokenService by inject()
    private val restClient: IRestClient by inject()
    private val eventAggregator: IMessagesCenter by inject()
    private val queueList: RequestQueueList by inject()
    private val constants: IConstant by inject()

    private val tag = "RestClientService: "
    val jsonSerializer = Json {
        ignoreUnknownKeys = true // ✅ ignore fields not in your data class
        isLenient = true              // allow relaxed JSON (optional)
        coerceInputValues = true      // replace missing/nulls with default
        prettyPrint = true
    }

    suspend inline fun <reified T> Get(restRequest: RestRequest): T
    {
        LogMethodStart("Get", restRequest)
        return WithApiErrorHandling()
        {
            MakeWebRequest(RestMethod.GET, restRequest)
        }
    }

    suspend inline fun <reified T> Post(restRequest: RestRequest): T
    {
        LogMethodStart("Post", restRequest)
        return WithApiErrorHandling()
        {
            MakeWebRequest(RestMethod.POST, restRequest)
        }
    }

    suspend inline fun <reified T> Put(restRequest: RestRequest)
    {
        LogMethodStart("Put", restRequest)
        return WithApiErrorHandling()
        {
            MakeWebRequest<Unit>(RestMethod.PUT, restRequest)
        }
    }

    suspend inline fun <reified T> Delete(restRequest: RestRequest): Any
    {
        LogMethodStart("Delete", restRequest)
        return WithApiErrorHandling()
        {
            MakeWebRequest<Any>(RestMethod.DELETE, restRequest)
        }
    }

    suspend inline fun <reified T> MakeWebRequest(method: RestMethod, restRequest: RestRequest): T
    {
        if (restRequest.WithBearer)
        {
            authTokenService.EnsureAuthValid()
        }

        val requestResult = AddRequestToQueue(method, restRequest).await()
        return Deserialize(requestResult)
    }

    fun AddRequestToQueue(method: RestMethod, restRequest: RestRequest): Deferred<String>
    {
        val path = GetUrlWithoutParam(restRequest.ApiEndpoint)
        val queueItemId = "${method.name}${path}/${restRequest.RequestPriority}/${restRequest.CancelSameRequest}"

        Log("Request $method: $queueItemId, priority: ${restRequest.RequestPriority} added to Queue")

        val item = RequestQueueItem().apply {
            Id = queueItemId;
            priority = restRequest.RequestPriority;
            timeoutType = restRequest.RequestTimeOut;
            parentList = queueList;
            RequestAction =
            {
                val fullUrl = "${constants.ServerUrlHost}${restRequest.ApiEndpoint}"
                val token = authTokenService.GetToken()
                val jsonBody = restRequest.RequestBody?.let { jsonSerializer.encodeToString(it)}

                val httpRequest = RestClientHttpRequest().apply()
                {
                    Url = fullUrl
                    RequestMethod = method
                    JsonBody = jsonBody
                    AccessToken = token
                }

                //log request start
                val requestSummary =
                    if (!httpRequest.JsonBody.isNullOrEmpty())
                        "DoHttpRequest(${httpRequest.RequestMethod}, ${httpRequest.Url}, ${httpRequest.JsonBody})"
                    else
                        "DoHttpRequest(${httpRequest.RequestMethod}, ${httpRequest.Url})"
                loggingService.LogMethodStarted(requestSummary)

                val responseContent = restClient.DoHttpRequest(method, httpRequest)

                //hide sensitivity data from logger
                val contentForLog = HideSensitiveData(responseContent)
                //log that request is finished
                loggingService.LogMethodFinished("$requestSummary with result: $contentForLog")

                responseContent
            }
            this.logger = this@RestService.loggingService
        }

        queueList.add(item)
        return item.CompletionSource
    }

    inline fun <reified T> Deserialize(jsonStr: String): T
    {
        // Detect API error response
        CheckForError(jsonStr)

        // Deserialize into T
        return jsonSerializer.decodeFromString<T>(jsonStr)
    }

    open fun CheckForError(jsonStr: String)
    {
        // Detect API error response
        if (jsonStr.contains("error:", ignoreCase = true))
        {
            val jsonObj = Json.parseToJsonElement(jsonStr) as? JsonObject
            val error = jsonObj?.get("error")?.jsonPrimitive?.content
            if (error != null) throw ServerApiException(error)
        }
    }

    suspend fun <T> WithApiErrorHandling(block: suspend () -> T): T
    {
        try
        {
            return block()
        }
        catch (ex: AuthExpiredException)
        {
            val authErrorEvent = eventAggregator.GetEvent<AuthErrorEvent> { AuthErrorEvent() };
            authErrorEvent.Publish(null)
            throw ex
        }
        catch (e: ConnectTimeoutException)
        {
            throw HttpConnectionException("Connection timed out", e)
        }
        catch (e: SocketTimeoutException)
        {
            throw HttpConnectionException("Request timed out", e)
        }
        catch (e: UnresolvedAddressException)
        {
            throw HttpConnectionException("Cannot resolve host", e)
        }
        catch (e: io.ktor.utils.io.errors.IOException)
        {
            throw HttpConnectionException("Network error: ${e.message}", e)
        }
        catch (e: RedirectResponseException) // 3xx error code
        {
            throw HttpRequestException(e.response.status.value, "HTTP error: ${e.response.status}", e)
        }
        catch (e: ClientRequestException)// 4xx error code
        {
            // Handle 401 Unauthorized
            if (e.response.status == HttpStatusCode.Unauthorized)
            {
                val authErrorEvent = eventAggregator.GetEvent<AuthErrorEvent> { AuthErrorEvent() };
                authErrorEvent.Publish(null)
            }
            throw HttpRequestException(e.response.status.value, "Client error: ${e.response.status}", e)
        }
        catch (e: ServerResponseException)// 5xx error code
        {
            throw HttpRequestException(e.response.status.value, "Server error: ${e.response.status}", e)
        }
        catch (e: ResponseException) // Other HTTP errors
        {
            throw HttpRequestException(e.response.status.value, "HTTP error: ${e.response.status}", e)
        }
    }

    private fun GetUrlWithoutParam(url: String): String
    {
        val hasQuery = url.contains("?")
        val urlWithoutParam = if (hasQuery) url.split("?")[0] else url
        val parts = urlWithoutParam.split("/")
        val count = if (hasQuery) parts.size else parts.size - 1

        val newUrl = buildString() {
            for (i in 0 until count)
            {
                val seg = parts[i]
                if (seg.isNotEmpty()) append("/$seg")
            }
        }
        return newUrl
    }

    private fun Log(message: String) = loggingService.Log("$tag$message")

    private fun HideSensitiveData(data: String): String {
        if (data.contains("access_token"))
        {
            return if (true)//PlatformUtils.isDebug)
            {
                data
            } else
            {
                try
                {
                    val json = Json.parseToJsonElement(data).jsonObject.toMutableMap()
                    val sensitiveKeys = listOf(
                        "access_token",
                        "userName",
                        "phoneNumber",
                        "token_type",
                        ".issued",
                        ".expires",
                        "expires_in"
                    )
                    sensitiveKeys.forEach { json.remove(it) }
                    jsonSerializer.encodeToString(json)
                }
                catch (e: Exception)
                {
                    data
                }
            }
        }
        return data
    }
}