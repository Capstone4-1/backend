package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.MailRoomInfo
import com.kmouit.capstone.domain.jpa.MailRoomInfoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface MailRoomInfoRepository : JpaRepository<MailRoomInfo, MailRoomInfoId> {

    fun findAllByIdMemberId(memberId: Long): List<MailRoomInfo>

    @Query("SELECT m.id.mailRoomId FROM MailRoomInfo m WHERE m.id.memberId = :memberId")
    fun findMailRoomIdsByMemberId(@Param("memberId") memberId: Long): List<Long>

    @Query(
        """
    SELECT mri.mailRoom.id
    FROM MailRoomInfo mri
    WHERE mri.member.id IN (:memberId1, :memberId2)
    GROUP BY mri.mailRoom.id
    HAVING COUNT(mri.mailRoom.id) = 2
"""
    )
    fun findRoomIdByTwoMembers(memberId1: Long, memberId2: Long): Long?


    fun existsByIdMailRoomIdAndIdMemberId(mailRoomId: Long, memberId: Long): Boolean
}