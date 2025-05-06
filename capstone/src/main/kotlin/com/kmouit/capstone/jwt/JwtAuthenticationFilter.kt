package com.kmouit.capstone.jwt

import com.kmouit.capstone.service.CustomUserDetailService
import io.jsonwebtoken.ExpiredJwtException
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
import java.security.SignatureException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
@Component
class JwtAuthenticationFilter(
    @Value("\${spring.jwt.secret}") private val secret: String,
    private val customUserDetailService: CustomUserDetailService  // ✅
) : OncePerRequestFilter() {

    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.jcaName
    )

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.substring(7)

            try {
                val claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .body
                val username = claims["username"] as String

                // ✅ 여기 핵심 수정
                val userDetails = customUserDetailService.loadUserByUsername(username)

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: ExpiredJwtException) {
                sendErrorResponse(response, "TOKEN_EXPIRED", "Access Token has expired")
                return
            } catch (e: SignatureException) {
                sendErrorResponse(response, "INVALID_TOKEN", "Invalid JWT signature")
                return
            } catch (e: Exception) {
                sendErrorResponse(response, "AUTH_ERROR", "Authentication failed")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun sendErrorResponse(response: HttpServletResponse, error: String, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"error": "$error", "message": "$message"}""")
    }
}
