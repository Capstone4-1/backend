package com.kmouit.capstone.api

import com.kmouit.capstone.domain.jpa.Banner
import com.kmouit.capstone.domain.jpa.BannerType
import com.kmouit.capstone.service.BannerService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/banners")
class BannerController(
    private val bannerService: BannerService,
) {
    // 저장 (완전 덮어쓰기)

    @PreAuthorize("hasRole('STUDENT_COUNCIL')")
    @PostMapping
    fun saveBanners(@RequestBody banners: List<BannerRequest>): ResponseEntity<Map<String, String>> {
        bannerService.saveAll(banners)
        return ResponseEntity.ok(mapOf("message" to "배너 저장 완료!"))
    }

    // 조회
    @GetMapping
    fun responseGetBanners(): ResponseEntity<BannerListResponse> {
        val banners = bannerService.getBanners()
        return ResponseEntity.ok(banners)
    }


    @PreAuthorize("hasRole('STUDENT_COUNCIL')")
    @DeleteMapping("/{id}")
    fun deleteBanner(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        bannerService.deleteOne(id)
        return ResponseEntity.ok(mapOf("message" to "배너 삭제 완료!"))
    }
}


data class BannerRequest(
    val title: String?,
    val targetUrl: String?,   // 일반 배너면 링크, 미디어면 videoUrl
    val bannerType: BannerType,
    val displayOrder: Int?,
    val content: String?,
)

data class BannerListResponse(
    val normalBanners: List<BannerResponse>, // BannerType.NORMAL
    val mediaBanners: List<BannerResponse>,   // BannerType.MEDIA
)

data class BannerResponse(
    val id: Long?,
    val title: String?,        // 일반 배너 제목, 미디어는 null 가능
    val targetUrl: String?,    // 링크 또는 영상 URL
    val bannerType: BannerType,
    val displayOrder: Int?,
    val content: String?,
) {
    companion object {
        fun from(banner: Banner): BannerResponse {
            return BannerResponse(
                id = banner.id,
                title = banner.title,
                targetUrl = banner.targetUrl,
                bannerType = banner.bannerType!!,
                displayOrder = banner.displayOrder,
                content = banner.content
            )
        }
    }
}