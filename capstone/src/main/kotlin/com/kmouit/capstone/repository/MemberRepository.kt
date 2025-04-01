package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {


    fun findByUsername(username: String): Member?
}