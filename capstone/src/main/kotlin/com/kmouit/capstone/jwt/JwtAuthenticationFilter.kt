package com.kmouit.capstone.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtAuthenticationFilter(
    @Value("\${spring.jwt.secret}") private val secret: String, // secret key to validate JWT signature
) : OncePerRequestFilter() {

    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.jcaName
    )

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val authorizationHeader = request.getHeader("Authorization")

        // Check if header contains Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.substring(7) // Extract token (remove "Bearer " prefix)

            try {
                // Parse the JWT and validate it
                val claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Set the key used to validate the JWT
                    .build()
                    .parseClaimsJws(token)
                    .body

                // Extract username and roles from claims
                val username = claims["username"] as String
                val roles = claims["role"] as List<String> // Extract roles (list of roles)

                // Convert roles to SimpleGrantedAuthority
                val authorities = roles.map { role -> SimpleGrantedAuthority(role) }

                // Create an authentication token
                val authentication: Authentication = UsernamePasswordAuthenticationToken(username, null, authorities)

                // Set authentication in the security context
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                // Handle token parsing error
                println("JWT parsing error: ${e.message}")
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response)
    }
}
