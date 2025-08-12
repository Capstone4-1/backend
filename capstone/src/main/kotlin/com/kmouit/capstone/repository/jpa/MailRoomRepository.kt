package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.MailRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface MailRoomRepository :JpaRepository<MailRoom, Long> {



    @Query("""
    SELECT DISTINCT mr 
    FROM MailRoom mr
    JOIN FETCH mr.mailRoomInfos mri
    JOIN FETCH mri.member m
    WHERE mr.id IN :roomIds
""")
    fun findAllWithMembersByRoomIds(@Param("roomIds") roomIds: List<Long>): List<MailRoom>
}