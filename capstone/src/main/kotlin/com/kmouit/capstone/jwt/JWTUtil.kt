package com.kmouit.capstone.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class JWTUtil (
    @Value("\${spring.jwt.secret}") secret: String
){

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

    fun getRole(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .get("role", String::class.java)
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

    fun createJwt(username: String, role: MutableList<out GrantedAuthority>, expiredMs: Long): String {
        return Jwts.builder()
            .claim("username", username)
            .claim("role", role.map { it.authority })
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }



}