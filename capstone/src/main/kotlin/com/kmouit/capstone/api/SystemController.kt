package com.kmouit.capstone.api

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.jpa.CornerType
import com.kmouit.capstone.domain.jpa.MealType
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.MenuService
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
    private val menuService: MenuService
) {

    //학교 공지
    @PostMapping("/crawling-notice/univ")
    fun saveUnivNotices(
        @RequestBody noticeList: List<CrawledNoticeDto>,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        postService.saveCrawledNotices(noticeList, userDetails.member.id!!, BoardType.NOTICE_UNIV)
        return ResponseEntity.ok(mapOf("message" to "학교공지 저장 완료 (${noticeList.size}건)"))
    }

    //학과 공지
    @PostMapping("/crawling-notice/dept")
    fun saveDeptNotices(
        @RequestBody noticeList: List<CrawledNoticeDto>,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        postService.saveCrawledNotices(noticeList, userDetails.member.id!!, BoardType.NOTICE_DEPT)
        return ResponseEntity.ok(mapOf("message" to "학과공지 저장 완료 (${noticeList.size}건)"))
    }

    @PostMapping("/crawling-menu")
    fun saveCrawledMenu(
        @RequestBody request: CrawledMenuRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        println("식단 컨트롤러 호출")
        for (item in request.items) {
            println("item = ${item.date}")
            println("item = ${item.studentCafeteria}")
            println("item = ${item.staffCafeteria}")
        }
        menuService.saveCrawledMenu(request.items)
        return ResponseEntity.ok(mapOf("message" to "크롤링 식단 저장 완료"))
    }
}
data class CrawledNoticeDto(
    val title: String,
    val content: String,
    val url: String,
    val date: LocalDate,
    val img: List<String>
)


data class CrawledMenuRequest(
    val items: List<MenuDayDTO>
)

data class MenuDayDTO(
    val date: LocalDate,
    val studentCafeteria: Map<CornerType, List<String>>?, // 학생식당
    val staffCafeteria: Map<MealType, List<String>>?  // 교직원식당
)