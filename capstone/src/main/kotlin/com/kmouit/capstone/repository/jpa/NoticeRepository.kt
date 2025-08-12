package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.Notice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoticeRepository :JpaRepository<Notice, Long> {
    fun findByMemberId(id: Long): MutableList<Notice>
}