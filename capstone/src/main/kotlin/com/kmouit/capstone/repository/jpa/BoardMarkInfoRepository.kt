package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.jpa.BoardMarkInfo
import com.kmouit.capstone.domain.jpa.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface BoardMarkInfoRepository : JpaRepository<BoardMarkInfo, Long> {
    fun findAllByMember(member: Member): List<BoardMarkInfo>

    @Query("SELECT b FROM BoardMarkInfo b JOIN FETCH b.member WHERE b.member.id = :memberId")
    fun findAllByMemberIdWithFetch(@Param("memberId") memberId: Long): List<BoardMarkInfo>
    fun existsByMemberAndBoardType(member: Member, boardType: BoardType): Boolean

    fun findByMemberAndBoardType(member: Member, boardType: BoardType): BoardMarkInfo?
    fun deleteAllByMember(member: Member)

}