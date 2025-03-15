package com.kmouit.capstone.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.kmouit.capstone.dtos.LoginForm
import com.kmouit.capstone.service.RefreshTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class LoginFilter(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil,
    private val refreshTokenService: RefreshTokenService
) : UsernamePasswordAuthenticationFilter() {

    init {
        setFilterProcessesUrl("/api/member/login")  //
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        println("===로그인필터 호출===")
        val loginForm = ObjectMapper().readValue(request.inputStream, LoginForm::class.java)
        val username = loginForm.username
        val password = loginForm.password

        // null이나 빈 값 체크 추가 가능
        if (username.isBlank() || password.isBlank()) {
            throw BadCredentialsException("Username or password cannot be empty")
        }

        val authToken = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(authToken)
    }


    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: FilterChain?,
        authResult: Authentication?
    ) {
        val userDetails = authResult?.principal as CustomUserDetails
        val roles = userDetails.authorities.map { it.authority } // GrantedAuthority에서 authority 값만 추출해서 리스트로 변환
        val accessToken = jwtUtil.createAccessToken(userDetails.username, roles) // roles를 전달
        val refreshToken = jwtUtil.createRefreshToken(userDetails.username)

        response?.contentType = "application/json"
        response?.writer?.write("""{"accessToken": "$accessToken", "refreshToken": "$refreshToken"}""")
        refreshTokenService.saveRefreshToken(userDetails.username, refreshToken) //메모리에 리프레시토큰 저장, 추후 레디스로 대체

    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        failed: AuthenticationException?
    ) {
        response?.status = HttpServletResponse.SC_UNAUTHORIZED
        response?.writer?.write("Invalid username or password")
    }
}
