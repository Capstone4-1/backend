package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.MailStatus
import com.kmouit.capstone.domain.jpa.Mail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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


    fun findWithMembersByMailRoomId(
        mailRoomId: Long,
        pageable: Pageable
    ): Page<Mail>

    fun findWithMembersByMailRoomIdAndIdLessThan(
        mailRoomId: Long,
        id: Long,
        pageable: Pageable
    ): List<Mail>

    @Query("SELECT m FROM Mail m WHERE m.mailRoom.id = :roomId AND m.id < :beforeId ORDER BY m.id DESC")
    fun findOldMails(@Param("roomId") roomId: Long, @Param("beforeId") beforeId: Long, pageable: Pageable): List<Mail>

}