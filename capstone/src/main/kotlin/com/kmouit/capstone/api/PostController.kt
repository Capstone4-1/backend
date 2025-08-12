package com.kmouit.capstone.api

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.jpa.LecturePostsDto
import com.kmouit.capstone.domain.jpa.SimplePostDto
import com.kmouit.capstone.jwt.CustomUserDetails
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
    private val commentService: CommentService,
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
        @AuthenticationPrincipal userDetails: CustomUserDetails,
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
        val resultPage: Page<SimplePostDto> =
            postService.findPostDtoByBoardType(userDetails.getId(), boardType, pageable, filter, query)

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
        @AuthenticationPrincipal user: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getTopLevelComments(postId, user.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "최상위 댓글 조회 성공",
                "comments" to comments
            )
        )
    }


    @GetMapping("/lecture/{lecturePostId}/comments")
    fun getTopLevelCommentsForLecturePost(
        @PathVariable lecturePostId: Long,
        @AuthenticationPrincipal user: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val comments = commentService.getTopLevelCommentsForLecturePost(lecturePostId, user.getId())

        return ResponseEntity.ok(
            mapOf(
                "message" to "강의 게시글 댓글 조회 성공",
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
        @AuthenticationPrincipal user: CustomUserDetails,
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


    @PostMapping("/summary-multi")
    fun getMultiBoardSummaries(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: SummaryRequest,
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.getId()
        val summaries =
            postService.getMultipleSummaries(request.types.map { BoardType.from(it)!! }, userId, request.pageSize)
        return ResponseEntity.ok(
            mapOf(
                "message" to "게시판 통합 요약 조회 성공",
                "Posts" to summaries
            )
        )
    }

    data class SummaryRequest(
        val types: List<String>,
        val pageSize: Int = 5,
    )


    @GetMapping("/{boardType}/summary")
    fun getBoardSummary(
        @PathVariable boardType: String,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val summary = postService.getSummary(BoardType.from(boardType), userDetails.getId(), pageSize)
        println("디버깅:${pageSize}")
        return ResponseEntity.ok(
            mapOf(
                "Posts" to summary,
                "message" to "${boardType}게시판 요약 정보 조회 성공"
            )
        )
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
        @RequestParam boardType: String,
    ): ResponseEntity<Map<String, String>> {
        postService.deleteBoardMarkInfo(userDetails.getId(), boardType)
        return ResponseEntity.ok(mapOf("message" to "즐겨찾기 삭제 성공"))
    }


    @PostMapping("/{postId}/like")
    fun responseLikePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val currentCount = postService.likePost(postId, userDetails.member.id!!)
        return ResponseEntity.ok(
            mapOf(
                "message" to "좋아요 성공",
                "currentCount" to currentCount
            )
        )
    }

    @PostMapping("/{postId}/unlike")
    fun responseUnlikePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val currentCount = postService.unlikePost(postId, userDetails.member.id!!)
        return ResponseEntity.ok(
            mapOf(
                "message" to "좋아요 취소 성공",
                "currentCount" to currentCount
            )
        )
    }


    //마이 페이지 내 내가 쓴글
    @GetMapping("/my-posts")
    fun getMyPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val myPostsPage = postService.getMyPosts(userDetails.member, page, pageSize)
        return ResponseEntity.ok(
            mapOf(
                "posts" to myPostsPage.content,
                "totalPages" to myPostsPage.totalPages,
                "totalElements" to myPostsPage.totalElements,
                "currentPage" to myPostsPage.number,
                "message" to "내가 쓴 글 불러오기 성공"
            )
        )
    }


    @GetMapping("/my-comments")
    fun getMyComments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val myPostsPage = postService.getMyComments(userDetails.member, page, pageSize)
        return ResponseEntity.ok(
            mapOf(
                "posts" to myPostsPage.content,
                "totalPages" to myPostsPage.totalPages,
                "totalElements" to myPostsPage.totalElements,
                "currentPage" to myPostsPage.number,
                "message" to "내가 쓴 댓글 불러오기 성공"
            )
        )
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
    val parentId: Long? = null,
    val targetUrl: String? = null,
)

data class LecturePostRequestDto(
    val lectureId: Long,
    val boardType: String,  // LECTURE_Q, LECTURE_N, LECTURE_REF, LECTURE_R
    val title: String,
    val content: String,
    val imageUrls: String? = null,
)