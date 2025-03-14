package com.kmouit.capstone.dtos



data class TokenResponse(
    val accessToken :String,
    val refreshToken :String
)

data class RefreshTokenRequest(
    val refreshToken :String
)