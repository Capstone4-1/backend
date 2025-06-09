package com.kmouit.capstone.service

import com.kmouit.capstone.api.CrawledJobDto
import com.kmouit.capstone.domain.JobInfo
import com.kmouit.capstone.repository.JobInfoRepository
import org.springframework.stereotype.Service


@Service
class JobService(
    private val jobInfoRepository: JobInfoRepository
) {
    fun saveCrawledJobs(jobList: List<CrawledJobDto>, memberId: Long) {
        val jobs = jobList.map {
            JobInfo(
                title = it.title,
                company = it.info.company,
                region = it.info.region,
                employmentType = it.info.employmentType,
                description = it.content.description,
                targetUrl = it.content.url
            )
        }
        jobInfoRepository.saveAll(jobs)
    }
}