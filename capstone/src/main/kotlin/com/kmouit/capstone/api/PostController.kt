package com.kmouit.capstone.api

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.CommentDto
import com.kmouit.capstone.domain.LecturePostsDto
import com.kmouit.capstone.domain.PostDto
import com.kmouit.capstone.domain.SimplePostDto
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.CommentService
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
    private val commentService: CommentService
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


    @PostMapping("/lecture-post-up")
    fun createLecturePost(
        @RequestBody requestDto: LecturePostRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        postService.createLecturePost(requestDto, userDetails.member)
        return ResponseEntity.ok(mapOf("message" to "lecture-post-up 성공"))
    }
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        postService.deletePost(postId, userDetails.member)
        return ResponseEntity.ok(mapOf("message" to "게시글 삭제 성공"))
    }


    /**
     * 게시글 페이지 조회
     */
    @GetMapping
    fun responsePage(
        @RequestParam boardType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) filter: String?,
        @RequestParam(required = false) query: String?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val resultPage: Page<SimplePostDto> = postService.findPostDtoByBoardType(boardType, pageable, filter, query)

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


    /**
     * 게시글 하나 가져오기
     */
    @GetMapping("/{id}")
    fun responseGetPost(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        val dto = postService.getPostDetail(id, userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "게시글 detail 조회 성공",
                "dto" to dto
            )
        )
    }

    /**
     * 댓글만 가져오기
     */
    @GetMapping("/{postId}/comments")
    fun getTopLevelComments(
        @PathVariable postId: Long,
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getTopLevelComments(postId, user.getId())

        return ResponseEntity.ok(
            mapOf(
                "message" to "최상위 댓글 조회 성공",
                "comments" to comments
            )
        )
    }

    /**
     * 대댓글 가져오기
     */
    @GetMapping("/{commentId}/replies")
    fun getReplies(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val replies = commentService.getReplies(commentId, user.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "대댓글 조회 성공",
                "replies" to replies
            )
        )
    }




    @GetMapping("/{lectureId}/{id}")
    fun responseGetLecturePost(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long,
        @PathVariable lectureId: Long,
    ): ResponseEntity<LecturePostsDto> {
        val dto = postService.getLecturePostDetail(id, userDetails.getId())
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
    val parentId: Long? = null
)

data class LecturePostRequestDto(
    val lectureId: Long,
    val boardType: String,  // LECTURE_Q, LECTURE_N, LECTURE_REF, LECTURE_R
    val title: String,
    val content: String,
    val imageUrls: String? = null
)