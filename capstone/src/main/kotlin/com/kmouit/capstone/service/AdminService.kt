package com.kmouit.capstone.service

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.stereotype.Service


@Service
class AdminService(
    private val memberRepository: MemberRepository
) {
    fun searchUsers(username: String?, name: String?, role: String?): List<Member> {
        val spec = MemberSpecification.search(username, name, role)
        return memberRepository.findAll(spec)
    }

}