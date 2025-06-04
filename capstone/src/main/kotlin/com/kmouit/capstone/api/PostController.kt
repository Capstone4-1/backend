package com.kmouit.capstone.api

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.PostDto
import com.kmouit.capstone.domain.SimplePostDto
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("api/post")
class PostController(
    private val postService: PostService,
) {

    @PostMapping("/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @RequestBody requestDto: CommentRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        postService.createComment(requestDto, postId, userDetails.member)
        return ResponseEntity.ok(mapOf("message" to "댓글 등록 성공"))
    }

    @PostMapping("/post-up")
    fun responsePostUp(
        @RequestBody requestDto: PostRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        postService.createPost(requestDto, userDetails.member)
        return ResponseEntity.ok(
            mapOf("message" to "post-up 성공")
        )
    }

    @PostMapping("/post-down")
    fun responsePostDown() {
    }


    @GetMapping
    fun responsePage(
        @RequestParam boardType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Map<String, Any>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val resultPage: Page<SimplePostDto> = postService.findPostDtoByBoardType(boardType, pageable)
        val response = PostPageResponseDto(
            posts = resultPage.content,
            totalCount = resultPage.totalElements,
            totalPages = resultPage.totalPages,
            currentPage = resultPage.number,
            pageSize = resultPage.size
        )

        return ResponseEntity.ok(
            mapOf(
                "message" to "페이지 정보 조회 성공",
                "pageResponse" to response
            )
        )
    }


    @GetMapping("/{id}")
    fun responseGetPost(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long,
    ): ResponseEntity<PostDto> {
        val dto = postService.getPostDetail(id, userDetails.getId())
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/summary-multiple")
    fun getMultipleBoardSummaries(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val result = mapOf(
            "message" to "게시판 전체 요약 정보 조회 성공",
            "Posts_notice_c" to postService.getSummary(BoardType.from("NOTICE_C"), userDetails.getId()),
            "Posts_free" to postService.getSummary(BoardType.from("FREE"), userDetails.getId()),
            "Posts_secret" to postService.getSummary(BoardType.from("SECRET"), userDetails.getId()),
            "Posts_review" to postService.getSummary(BoardType.from("REVIEW"), userDetails.getId()),
            "Posts_market" to postService.getSummary(BoardType.from("MARKET"), userDetails.getId())
        )
        return ResponseEntity.ok(result)
    }

}

data class PostPageResponseDto(
    var posts: List<SimplePostDto>,
    var totalCount: Long,
    var totalPages: Int,
    var currentPage: Int,
    var pageSize: Int,
)

data class PostRequestDto(
    var boardType: String,
    var title: String,
    var content: String,
    var imageUrls: String? = null,
    var price: Int? = null,
)

data class CommentRequestDto(
    var content: String,
)