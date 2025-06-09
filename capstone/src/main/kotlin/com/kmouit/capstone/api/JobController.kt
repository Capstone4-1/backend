package com.kmouit.capstone.api

import com.kmouit.capstone.domain.JobInfoDto
import com.kmouit.capstone.repository.JobInfoRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/jobs")
class JobController(
    private val jobInfoRepository: JobInfoRepository
) {
    @GetMapping
    fun getJobInfo(): List<JobInfoDto> {
        val findAll = jobInfoRepository.findAll()
        return findAll.map { JobInfoDto.from(it) }
    }

}