package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername ( username :String) : Member?
    fun findByEmail ( email :String) : Member?
}