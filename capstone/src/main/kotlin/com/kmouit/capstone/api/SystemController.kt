package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.JobService
import com.kmouit.capstone.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@PreAuthorize("hasRole('SYSTEM')")
@RestController
@RequestMapping("/api/system")
class SystemController(
    private val postService: PostService,
    private val jobService: JobService
) {
    @PostMapping("/crawling-notice")
    fun saveCrawledNotices(
        @RequestBody noticeList: List<CrawledNoticeDto>,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {

        postService.saveCrawledNotices(noticeList, userDetails.member.id!!)
        return ResponseEntity.ok(mapOf("message" to "크롤링 공지사항 저장 완료 (${noticeList.size}건)"))
    }

    @PostMapping("/crawling-job")
    fun saveCrawledJobInfo(
        @RequestBody jobList: List<CrawledJobDto>,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        jobService.saveCrawledJobs(jobList, userDetails.member.id!!)
        return ResponseEntity.ok(mapOf("message" to "크롤링된 채용공고 저장 완료 (${jobList.size}건)"))
    }
}
data class CrawledNoticeDto(
    val title: String,
    val content: String,
    val url: String,
    val date: LocalDate,
    val img: List<String>
)

data class CrawledJobDto(
    val title: String,
    val info: JobDetailDto,
    val content: JobContentDto
)

data class JobDetailDto(
    val region: String,
    val employmentType: String,
    val company: String,
    val deadline : LocalDate?
)

data class JobContentDto(
    val description: String,
    val url: String
)