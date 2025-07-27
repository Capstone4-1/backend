package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.ScheduleInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleInfoRepository : JpaRepository<ScheduleInfo, Long> {
}