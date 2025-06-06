package com.kmouit.capstone.api

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.PostDto
import com.kmouit.capstone.domain.SimplePostDto
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("api/post")
class PostController(
    private val postService: PostService,
    private val memberRepository: MemberRepository,
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
        @AuthenticationPrincipal userDetails: CustomUserDetails,
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
        val isMarked = postService.checkBoardMark(boardType, userDetails.getId())

        return ResponseEntity.ok(
            mapOf(
                "message" to "페이지 정보 조회 성공",
                "pageResponse" to response,
                "isMarked" to isMarked
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
            "Posts_market" to postService.getSummary(BoardType.from("MARKET"), userDetails.getId()),
            "Posts_notice" to postService.getSummary(BoardType.from("NOTICE"), userDetails.getId())
        )
        return ResponseEntity.ok(result)
    }


    @GetMapping("/favorites")
    fun getMyFavorites(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val result = postService.findMyFavorites(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "my 즐겨찾기 조회 성공",
                "favorites" to result
            )
        )
    }

    @PostMapping("/favorites")
    fun responseSaveBoardMark(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: FavoriteRequest,
    ): ResponseEntity<Map<String, String>> {
        postService.saveBoardMarkInfo(userDetails.getId(), request.boardType)
        return ResponseEntity.ok(
            mapOf(
                "message" to "my 즐겨찾기 추가 성공",
            )
        )
    }

    @DeleteMapping("/favorites")
    fun responseDeleteBoardMark(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam boardType: String
    ): ResponseEntity<Map<String, String>> {
        postService.deleteBoardMarkInfo(userDetails.getId(), boardType)
        return ResponseEntity.ok(mapOf("message" to "즐겨찾기 삭제 성공"))
    }
}

data class FavoriteRequest(
    val boardType: String,
)

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