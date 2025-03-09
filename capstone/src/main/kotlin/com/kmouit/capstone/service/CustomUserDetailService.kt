package com.kmouit.capstone.service

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val memberRepository: MemberRepository
):UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val userData = username?.let { memberRepository.findByUsername(it) }
            ?: throw UsernameNotFoundException("User not found with username: $username")
        return CustomUserDetails(userData)
    }

}