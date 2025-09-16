package com.kmouit.capstone.config


import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.jwt.JwtAuthenticationFilter
import com.kmouit.capstone.jwt.LoginFilter
import com.kmouit.capstone.service.RefreshTokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
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
    fun roleHierarchy(): RoleHierarchy {
        return RoleHierarchyImpl.fromHierarchy(
            """
        ROLE_SYSTEM > ROLE_ADMIN
        ROLE_ADMIN > ROLE_PROFESSOR
        ROLE_PROFESSOR > ROLE_MANAGER
        ROLE_MANAGER > ROLE_STUDENT_COUNCIL
        ROLE_STUDENT_COUNCIL > ROLE_STUDENT
        ROLE_STUDENT > ROLE_USER
        """.trimIndent()
        )
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        configuration: AuthenticationConfiguration,
    ): AuthenticationManager = configuration.authenticationManager

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
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/member/join").permitAll()
                    .requestMatchers("/api/auth/email-check").permitAll()
                    .requestMatchers("/api/auth/verify-code").permitAll()
                    .requestMatchers("/api/auth/check-duplicate").permitAll()
                    .requestMatchers("/api/member/login").permitAll()
                    .requestMatchers("/api/member/verify-id").permitAll()
                    .requestMatchers("/api/member/reset-password/no-login").permitAll()
                    .requestMatchers("/api/member/admin-test").hasAnyRole("ADMIN")
                    .requestMatchers("/api/auth/refresh").permitAll()
                    .requestMatchers("/api/health").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterAfter(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}
