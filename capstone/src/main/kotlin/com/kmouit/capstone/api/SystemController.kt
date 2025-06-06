package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
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
    private val postService: PostService
) {
    @PostMapping("/crawling-notice")
    fun saveCrawledNotices(
        @RequestBody noticeList: List<CrawledNoticeDto>,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {

        postService.saveCrawledNotices(noticeList, userDetails.member.id!!)
        return ResponseEntity.ok(mapOf("message" to "크롤링 공지사항 저장 완료 (${noticeList.size}건)"))
    }
}
data class CrawledNoticeDto(
    val title: String,
    val content: String,
    val url: String,
    val date: LocalDate,
    val img: List<String>
)
