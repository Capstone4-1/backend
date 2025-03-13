package com.kmouit.capstone.config


import com.kmouit.capstone.jwt.JwtAuthenticationFilter
import com.kmouit.capstone.jwt.LoginFilter
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
        jwtAuthenticationFilter: JwtAuthenticationFilter, // com.kmouit.capstone.jwt.JwtAuthenticationFilter 주입
    ): SecurityFilterChain {
        http
            .cors {  }
            .csrf { it.disable() } // CSRF 비활성화 (테스트용)
            .formLogin { it.disable() } // 기본 로그인 폼 비활성화 (필요에 따라 설정)
            .httpBasic { it.disable() } // 기본 HTTP 인증 비활성화
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/member/join").permitAll() // 회원가입 API 허용
                    .requestMatchers("/api/member/login").permitAll() // 로그인 API 허용
                    .requestMatchers("/api/member/admin-test").hasAnyRole("ADMIN")
                    .anyRequest().authenticated() // 나머지는 인증 필요
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            .addFilterAfter(LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}