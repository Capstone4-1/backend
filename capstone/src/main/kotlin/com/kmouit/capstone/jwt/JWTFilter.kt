package com.kmouit.capstone.jwt

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.service.CustomUserDetailService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JWTFilter(
    private val jwtUtil: JWTUtil,
    private val customUserDetailService: CustomUserDetailService
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


        val userDetails = customUserDetailService.loadUserByUsername(username)



        val authToken = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authToken

        filterChain.doFilter(request, response)
    }
}
