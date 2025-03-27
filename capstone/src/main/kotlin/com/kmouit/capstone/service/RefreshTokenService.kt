package com.kmouit.capstone.service

import org.springframework.stereotype.Service

@Service
class RefreshTokenService { //개발시엔 그냥 메모리에 저장, 추후 redis 최적화 고려
    private val tokenStorage = mutableMapOf<String, String>()

    fun saveRefreshToken(username: String, refreshToken: String) {
        tokenStorage[username] = refreshToken
    }

    fun getRefreshToken(username: String): String? {
        return tokenStorage[username]
    }

    fun deleteRefreshToken(username: String) {
        tokenStorage.remove(username)
    }
}