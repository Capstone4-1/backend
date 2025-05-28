package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.CrawledNotice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository



@Repository
interface CrawledNoticeRepository : JpaRepository<CrawledNotice, Long> {
}