package com.kmouit.capstone.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JWTUtil(
    @Value("\${spring.jwt.secret}") secret: String,
    @Value("\${spring.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${spring.jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long
) {


    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.jcaName
    )

    fun getUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .get("username", String::class.java)
    }

    fun getRole(token: String): List<String> {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .get("role", List::class.java) as List<String> // role을 List<String>으로 반환
    }

    fun isExpired(token: String): Boolean {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .expiration
            .before(Date())
    }

    fun createAccessToken(id:Long,name:String,username: String, role: List<String>, expiredMs: Long = accessTokenExpiration): String {
        return Jwts.builder()
            .claim("id", id)
            .claim("name", name)
            .claim("username", username)
            .claim("role", role)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun createRefreshToken(id:Long, username: String, expiredMs: Long = refreshTokenExpiration): String {
        return Jwts.builder()
            .claim("id", id)
            .claim("username", username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }
}
