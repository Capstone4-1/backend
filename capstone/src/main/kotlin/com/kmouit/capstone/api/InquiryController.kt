package com.kmouit.capstone.api

import com.kmouit.capstone.domain.jpa.InquiryItem
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.jpa.InquiryItemRepository
import com.kmouit.capstone.service.InquiryService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/inquiry")
class InquiryController(
    private val inquiryService: InquiryService,
) {
    @PostMapping
    fun createInquiry(
        @RequestBody request: InquiryRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val inquiry: InquiryItem = inquiryService.createInquiry(request, userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "문의 등록 성공",
            )
        )
    }
}

data class InquiryRequest(
    val title: String,
    val content: String,
)