package com.kmouit.capstone.jwt

import com.kmouit.capstone.domain.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val member: Member
) : UserDetails {

    override fun getAuthorities(): MutableList<out GrantedAuthority> {
        return member.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
            .toMutableList()
    }

    override fun getPassword(): String = member.password!!

    override fun getUsername(): String = member.username!!

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
