package com.kmouit.capstone.service

import com.kmouit.capstone.Role.ADMIN
import com.kmouit.capstone.Role.USER
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class MemberManageService(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val memberRepository: MemberRepository
) {
    fun join(joinForm: JoinForm){
        if(memberRepository.findByUsername(joinForm.username)!= null){
            throw DuplicateUsernameException("이미 가입된 id 입니다")
        }

        val member = Member(
            username = joinForm.username,
            password = passwordEncoder.encode(joinForm.password),
            name = joinForm.name,
            email = joinForm.email,
        )
        member.roles.add(USER)
        member.roles.add(ADMIN)

        memberRepository.save(member)
    }



}