package com.kmouit.capstone.api

import com.kmouit.capstone.InquiryCategory
import com.kmouit.capstone.Role
import com.kmouit.capstone.domain.jpa.InquiryItem
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.InquiryDto
import com.kmouit.capstone.service.InquiryService
import org.springframework.data.domain.Page
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api")
class InquiryController(
    private val inquiryService: InquiryService,
    private val assembler: PagedResourcesAssembler<InquiryDto>,
) {
    @PostMapping("/inquiry")
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


    @GetMapping("/inquiries")
    fun getInquiries(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam(required = false) state: String?,
    ): PagedModel<EntityModel<InquiryDto>> {
        val pageResult: Page<InquiryDto> = inquiryService.getInquiries(page, size, state)
        return assembler.toModel(pageResult)
    }

    @PostMapping("/inquiry/{id}/complete")
    fun completeInquiry(
        @PathVariable id: Long,
        @RequestBody request: CompleteRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        inquiryService.completeInquiry(id, request.answer, userDetails.member)
        return ResponseEntity.ok(mapOf("message" to "문의 완료 처리 및 답변 저장 완료"))
    }

}

data class InquiryRequest(
    val title: String,
    val content: String,
    val category: InquiryCategory, // 문의 유형
    val targetRole: Role? = null, // 권한 요청일 때만 사용
)

data class CompleteRequest(
    val answer: String
)