package com.ccxiaoji.app.data.remote.api

import com.ccxiaoji.app.data.remote.dto.LoginRequest
import com.ccxiaoji.app.data.remote.dto.LoginResponse
import com.ccxiaoji.app.data.remote.dto.RefreshTokenRequest
import com.ccxiaoji.app.data.remote.dto.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
    
    @POST("v1/auth/logout")
    suspend fun logout(): Response<Unit>
}