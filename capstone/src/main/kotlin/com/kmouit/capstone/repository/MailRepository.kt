package com.kmouit.capstone.repository

import com.kmouit.capstone.MailStatus
import com.kmouit.capstone.domain.Mail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface MailRepository :JpaRepository<Mail, Long> {
    fun countByReceiverIdAndStatus(memberId: Long, new: MailStatus): Int
}