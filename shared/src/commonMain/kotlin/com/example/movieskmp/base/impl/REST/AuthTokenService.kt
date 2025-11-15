package com.base.impl.REST

import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.REST.AuthTokenDetails
import com.base.abstractions.REST.Exceptions.AuthExpiredException
import com.base.abstractions.REST.IAuthTokenService
import com.base.impl.Diagnostic.LoggableService
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
//import kotlinx.datetime.minus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import kotlin.getValue

internal class AuthTokenService : LoggableService(), IAuthTokenService
{
    private val preferencesService: IPreferences by inject()

    companion object
    {
        private const val TAG = "AuthTokenService: "
        private const val AUTH_KEY = "user_at"
    }

    private var authToken: AuthTokenDetails? = null

    override suspend fun GetToken(): String?
    {
        val authToken = GetAuthTokenDetails()
        return authToken?.Token ?: ""
    }

    override suspend fun EnsureAuthValid()
    {
        if(authToken == null)
        {
            authToken = GetAuthTokenDetails()
        }
        if (authToken == null)
        {
            loggingService.LogWarning("${TAG}Skip checking access token because authToken is null")
            return
        }
        val expireDate = authToken!!.ExpiredDate
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        //Let's throw expired when there are only two days left
        //This ensures user is prompted to re-authenticate before real expiration
        val expireMinus2Days = expireDate.minus(2, DateTimeUnit.DAY)
        if (expireMinus2Days < nowDate)
        {
            loggingService.LogWarning("${TAG}Access token is expired(expiredDate - 2days) $expireMinus2Days < $nowDate, actual expired date: $expireDate")
            throw AuthExpiredException()
        }
    }

    override suspend fun SaveAuthTokenDetails(authToken: AuthTokenDetails?)
    {
        val json = Json.encodeToString(authToken)
        preferencesService.Set(AUTH_KEY, json)
    }

    override suspend fun GetAuthTokenDetails(): AuthTokenDetails?
    {
        val authTokenJson = preferencesService.Get(AUTH_KEY, "")
        if (!authTokenJson.isNullOrEmpty())
        {
            return try
            {
                Json.decodeFromString<AuthTokenDetails>(authTokenJson)
            }
            catch (ex: Exception)
            {
                loggingService.LogError(ex, "${TAG}Failed to deserialize AuthTokenDetails")
                null
            }
        }
        return null
    }

}