package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Notice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NoticeRepository :JpaRepository<Notice, Long> {
    fun findByMemberId(id: Long): MutableList<Notice>
}