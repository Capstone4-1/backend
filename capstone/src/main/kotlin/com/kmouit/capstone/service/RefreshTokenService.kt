package com.kmouit.capstone.service

import com.kmouit.capstone.domain.redis.RefreshToken
import com.kmouit.capstone.repository.redis.RefreshTokenRepository
import org.springframework.stereotype.Service
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun saveRefreshToken(username: String, refreshToken: String) {
        refreshTokenRepository.save(RefreshToken(username, refreshToken))
    }

    fun getRefreshToken(username: String): String? {
        return refreshTokenRepository.findById(username)
            .map { it.token }
            .orElse(null)
    }

    fun deleteRefreshToken(username: String) {
        refreshTokenRepository.deleteById(username)
    }
}