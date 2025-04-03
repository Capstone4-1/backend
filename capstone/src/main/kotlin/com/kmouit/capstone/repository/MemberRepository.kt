package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> {


    fun findByUsername(username: String): Member?

    @Query(
        "SELECT DISTINCT m FROM Member m LEFT JOIN FETCH m.notices WHERE m.id = :id"
    )
    fun findMemberAndNoticeById(id: Long): Optional<Member>
}