package com.kmouit.capstone.api

import com.kmouit.capstone.domain.PostDto
import com.kmouit.capstone.domain.Posts
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
@PreAuthorize("permitAll()")
@RestController
@RequestMapping("api/post")
class PostController(
    private val postService: PostService
) {
    @PostMapping("/post-up")
    fun responsePostUp(
        @RequestBody requestDto: PostRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        postService.createPost(requestDto, userDetails.member)
        return ResponseEntity.ok(
            mapOf("message" to "post-up 성공")
        )
    }

    @PostMapping("post-down")
    fun responsePostDown(){
    }


    @GetMapping()
    fun responsePage(
        @RequestParam boardType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<PostDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        return postService.findAllByBoardType(boardType, pageable)
    }
}

data class PostRequestDto(
    val boardType: String,
    val title: String,
    val content: String
)