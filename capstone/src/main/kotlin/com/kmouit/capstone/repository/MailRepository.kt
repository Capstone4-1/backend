package com.kmouit.capstone.repository

import com.kmouit.capstone.MailStatus
import com.kmouit.capstone.domain.Mail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface MailRepository : JpaRepository<Mail, Long> {
    fun countByReceiverIdAndStatus(memberId: Long, new: MailStatus): Int

    @Query(
        """
        SELECT COUNT(m) 
        FROM Mail m 
        WHERE m.mailRoom.id = :roomId AND m.receiver.id = :memberId AND m.status = 'NEW'
        """
    )
    fun countNewMailsByRoomIdAndReceiverId(roomId: Long, memberId: Long): Int


    @Query("""
    SELECT m FROM Mail m 
    WHERE m.mailRoom.id = :roomId AND m.receiver.id = :receiverId AND m.status = 'NEW'
    """)
    fun findNewMailsByRoomIdAndReceiverId(roomId: Long, receiverId: Long): List<Mail>

}