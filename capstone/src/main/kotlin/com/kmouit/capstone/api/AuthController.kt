package com.kmouit.capstone.api

import com.kmouit.capstone.VerificationResult
import com.kmouit.capstone.config.AuthMailService
import com.kmouit.capstone.dtos.RefreshTokenRequest
import com.kmouit.capstone.dtos.TokenResponse
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.service.CustomUserDetailService
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.RefreshTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.contracts.contract


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val refreshTokenService: RefreshTokenService,
    private val jwtUtil: JWTUtil,
    private val customUserDetailService: CustomUserDetailService,
    private val memberManageService: MemberManageService,
    private val mailService: AuthMailService,
) {
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
        val newAccessToken = jwtUtil.createAccessToken(id, name, username, roles)
        val newRefreshToken = jwtUtil.createRefreshToken(id, username)

        refreshTokenService.saveRefreshToken(username, newRefreshToken)
        memberManageService.refreshRecentLoginTime(userDetails.getId())
        return ResponseEntity.ok(TokenResponse(newAccessToken, newRefreshToken))
    }

    @PostMapping("/email-check")
    fun emailCheck(
        @RequestBody mailDto: MailDto,
    ): ResponseEntity<Map<String, String>> {
        if (memberManageService.isEmailRegistered(mailDto.email)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "이미 가입된 이메일입니다."))
        }

        mailService.sendSimpleMessage(mailDto.email)
        return ResponseEntity.ok(mapOf("message" to "인증코드 송신 성공"))
    }

    data class MailDto(
        var email: String,
    )


    @PostMapping("/verify-code")
    fun verifyCode(
        @RequestBody verifyDto: VerifyDto,
    ): ResponseEntity<Map<String, String>> {
        return when (val result = mailService.verifyCode(verifyDto.email, verifyDto.code)) {
            VerificationResult.SUCCESS -> {
                ResponseEntity.ok(mapOf("message" to "인증 성공"))
            }

            VerificationResult.INVALID_CODE -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to "인증코드가 올바르지 않습니다."))
            }

            VerificationResult.EXPIRED -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to "만료된 인증코드입니다."))
            }
        }
    }

    @PostMapping("/check-duplicate")
    fun checkDuplicate(
        @RequestBody request: DuplicateCheckRequest,
    ): ResponseEntity<Map<String, Any>> {
        val errors = mutableListOf<Map<String, String>>()

        if (memberManageService.isUsernameRegistered(request.username)) {
            errors.add(mapOf("field" to "username", "message" to "이미 사용 중인 아이디입니다."))
        }

        if (memberManageService.isEmailRegistered(request.email)) {
            errors.add(mapOf("field" to "email", "message" to "이미 가입된 이메일입니다."))
        }

        if (memberManageService.isNicknameRegistered(request.nickname)) {
            errors.add(mapOf("field" to "nickname", "message" to "이미 사용 중인 닉네임입니다."))
        }

        if (errors.isNotEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("errors" to errors))
        }

        // 중복 없음 → 이메일 인증 발송
        mailService.sendSimpleMessage(request.email)
        return ResponseEntity.ok(mapOf("message" to "중복 없음, 인증코드 송신 성공"))
    }

    data class DuplicateCheckRequest(
        var username: String,
        var email: String,
        var nickname: String,
    )

    data class VerifyDto(
        var email: String,
        var code: String,
    )

}
