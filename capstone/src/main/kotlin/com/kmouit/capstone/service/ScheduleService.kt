package com.kmouit.capstone.service

import com.kmouit.capstone.repository.jpa.ScheduleInfoRepository
import org.springframework.stereotype.Service


@Service
class ScheduleService(
    private val scheduleInfoRepository: ScheduleInfoRepository
) {

}