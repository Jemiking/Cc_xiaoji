package com.ccxiaoji.shared.user.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ccxiaoji.shared.user.domain.model.User

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserDto
)

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("created_at")
    val createdAt: Long
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            email = email,
            createdAt = createdAt
        )
    }
}

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)