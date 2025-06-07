package com.kmouit.capstone.service

import com.kmouit.capstone.Role
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AdminService(
    private val memberRepository: MemberRepository
) {
    fun searchUsers(username: String?, name: String?, role: String?): List<Member> {
        val spec = MemberSpecification.search(username, name, role)
        return memberRepository.findAll(spec)
    }

    @Transactional
    fun grantRole(userId: Long, role: String) {
        val member = memberRepository.findById(userId)
            .orElseThrow { NoSuchElementException("회원을 찾을 수 없습니다.") }

        val newRole = Role.from(role)
        if (member.roles.any { it == newRole }) {
            throw IllegalArgumentException("이미 해당 권한이 있습니다.")
        }

        member.roles.add(newRole!!)
    }

    @Transactional
    fun revokeRole(userId: Long, role: String) {
        val member = memberRepository.findById(userId)
            .orElseThrow { NoSuchElementException("회원을 찾을 수 없습니다.") }

        val revokeTarget = member.roles.find { it.value == role }
            ?: throw IllegalArgumentException("해당 권한이 존재하지 않습니다.")

        member.roles.remove(revokeTarget)
    }

}