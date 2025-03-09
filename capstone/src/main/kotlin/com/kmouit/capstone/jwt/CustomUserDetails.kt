package com.kmouit.capstone.jwt

import com.kmouit.capstone.domain.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val member: Member
) :UserDetails{
    override fun getAuthorities(): MutableList<out GrantedAuthority> {
        println("--------------------")
        // 예시: roles가 단일 문자열이거나 쉼표로 구분된 여러 역할일 경우
        return member.roles!!.split(",")  // roles를 쉼표로 분리하여 각 역할에 대해 GrantedAuthority를 반환
            .map { SimpleGrantedAuthority(it.trim()) }  // 각 역할을 SimpleGrantedAuthority로 변환
            .toMutableList()
    }
    override fun getPassword(): String? {
        return member.password
    }

    override fun getUsername(): String? {
        return member.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}