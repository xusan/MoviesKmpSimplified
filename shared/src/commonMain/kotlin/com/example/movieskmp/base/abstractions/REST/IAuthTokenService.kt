package com.base.abstractions.REST

import kotlinx.datetime.LocalDate

interface IAuthTokenService
{
    suspend fun GetToken(): String?
    suspend fun EnsureAuthValid()
    suspend fun SaveAuthTokenDetails(authToken: AuthTokenDetails?)
    suspend fun GetAuthTokenDetails(): AuthTokenDetails?
}

data class AuthTokenDetails(val Token: String, val ExpiredDate: LocalDate, val RefreshToken: String)
