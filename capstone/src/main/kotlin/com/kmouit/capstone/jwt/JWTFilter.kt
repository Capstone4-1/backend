package com.kmouit.capstone.jwt

import com.kmouit.capstone.domain.Member
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JWTFilter(
    private val jwtUtil: JWTUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorization = request.getHeader("Authorization")
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            println("token null")
            filterChain.doFilter(request, response)
            return
        }

        println("authorization now")
        val token = authorization.split(" ")[1]

        if (jwtUtil.isExpired(token)) {
            println("token expired")
            filterChain.doFilter(request, response)
            return
        }

        val username = jwtUtil.getUsername(token)
        val role = jwtUtil.getRole(token)

        // UserEntity 생성 및 값 주입 (여기서는 임시로 UserEntity 객체를 생성, 실제로는 DB에서 조회 필요)
        val userEntity = Member().apply {
            this.username = username
            this.password = "temp"  // 비밀번호는 필요 없음
            this.roles = role  // 역할을 적절한 방식으로 설정
        }

        val customUserDetails = CustomUserDetails(userEntity)

        val authToken = UsernamePasswordAuthenticationToken(
            customUserDetails,
            null,
            customUserDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authToken

        filterChain.doFilter(request, response)
    }
}
