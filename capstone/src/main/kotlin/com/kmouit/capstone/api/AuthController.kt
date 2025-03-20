package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.RefreshTokenRequest
import com.kmouit.capstone.dtos.TokenResponse
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.service.CustomUserDetailService
import com.kmouit.capstone.service.RefreshTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/auth")
class AuthController (
    private val refreshTokenService: RefreshTokenService,
    private val jwtUtil: JWTUtil,
    private val customUserDetailService: CustomUserDetailService
){
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<Any> {
        val refreshToken = request.refreshToken

        val username = try {
            jwtUtil.getUsername(refreshToken)  // 리프레시 토큰에서 username 추출
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")
        }

        // 서버에서 리프레시 토큰이 저장되어 있는지 확인
        val storedRefreshToken = refreshTokenService.getRefreshToken(username)
        if (storedRefreshToken != refreshToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")
        }

        val userDetails = customUserDetailService.loadUserByUsername(username) as CustomUserDetails
        val roles = userDetails.authorities.map { it.authority }
        val id = userDetails.getId()
        val name = userDetails.getName()
        val newAccessToken = jwtUtil.createAccessToken(id,name, username, roles)
        val newRefreshToken = jwtUtil.createRefreshToken(id,username) // 7일 유효

        refreshTokenService.saveRefreshToken(username, newRefreshToken)

        return ResponseEntity.ok(TokenResponse(newAccessToken, newRefreshToken))
    }

}