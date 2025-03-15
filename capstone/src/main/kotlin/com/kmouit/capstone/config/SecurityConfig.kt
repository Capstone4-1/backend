package com.kmouit.capstone.config


import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.jwt.JwtAuthenticationFilter
import com.kmouit.capstone.jwt.LoginFilter
import com.kmouit.capstone.service.RefreshTokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtUtil: JWTUtil,
    private val refreshTokenService: RefreshTokenService
) {


    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }



    @Bean
    fun authenticationManager(
        configuration: AuthenticationConfiguration,
    ): AuthenticationManager {
        return configuration.authenticationManager
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationConfiguration: AuthenticationConfiguration,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
    ): SecurityFilterChain {
        http
            .cors {  }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/member/join").permitAll()
                    .requestMatchers("/api/member/login").permitAll()
                    .requestMatchers("/api/member/admin-test").hasAnyRole("ADMIN")
                    .requestMatchers("/auth/refresh").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 🚀 LoginFilter를 UsernamePasswordAuthenticationFilter 전에 실행
            .addFilterBefore(LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil,refreshTokenService), UsernamePasswordAuthenticationFilter::class.java)

            // 🚀 JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이후에 실행
            .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}