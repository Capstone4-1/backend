package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.LoginForm
import com.kmouit.capstone.dtos.TokenResponse
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.RefreshTokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberManageService: MemberManageService,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager,
    private val refreshTokenService: RefreshTokenService
) {


    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admin-test")
    fun test(request: HttpServletRequest): ResponseEntity<String> { //테스트 api
        val authorizationHeader  = request.getHeader("Authorization")
        if (authorizationHeader != null){
            println("======${authorizationHeader}")
        }else{
            println("버그")
        }
        return ResponseEntity.ok("관리자 하이")
    }


    @PostMapping("/join")
    fun join(@RequestBody joinForm: JoinForm): ResponseEntity<String> {
        println("회원가입 호출")
        try {
            memberManageService.join(joinForm)
        } catch (e: DuplicateUsernameException) {

        }
        return ResponseEntity.ok("회원 가입 성공")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginForm: LoginForm): ResponseEntity<Any> {
        println("===로그인 호출===")
        println(loginForm)
        return ResponseEntity.ok("로그인 요청 받음")
    }


    @PostMapping("/logout")
    fun logout(@RequestBody logoutRequest: Map<String, String>): ResponseEntity<Any> {
        val refreshToken = logoutRequest["refreshToken"] ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is required")

        try {
            val username = jwtUtil.getUsername(refreshToken)
            refreshTokenService.deleteRefreshToken(username)
            return ResponseEntity.ok("Logged out successfully")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")
        }
    }
}