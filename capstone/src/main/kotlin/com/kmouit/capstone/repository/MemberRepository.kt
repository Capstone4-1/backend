package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> , JpaSpecificationExecutor<Member>{


    fun findByUsername(username: String): Member?

    @Query(
        "SELECT DISTINCT m " +
                "FROM Member m " +
                "LEFT JOIN FETCH m.notices n " +
                "WHERE m.id = :id AND n.status = 'UNREAD'"
    )
    fun findMemberAndUnreadNoticesById(id: Long): Member?
    fun existsByNickname(nickname: String): Boolean


    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.notices WHERE m.id = :id")
    fun findMemberAndNoticesById(@Param("id") id: Long): Member?
    fun findByNickname(nickname: String): Member?
    fun existsByEmail(email: String): Boolean

}