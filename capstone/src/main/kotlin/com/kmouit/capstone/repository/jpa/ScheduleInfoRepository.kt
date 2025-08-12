package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.ScheduleInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleInfoRepository : JpaRepository<ScheduleInfo, Long> {
}