package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@PreAuthorize("hasRole('SYSTEM')")
@RestController
@RequestMapping("/api/system")
class SystemController {
    @PostMapping("/crawling-notice")
    fun putCrawlingNotice(
        @RequestBody requestDto: PostRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity.ok(
            mapOf("message" to "post-up 성공")
        )
    }


}