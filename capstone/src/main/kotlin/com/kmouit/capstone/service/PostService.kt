package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.api.CrawledNoticeDto
import com.kmouit.capstone.api.LecturePostRequestDto
import com.kmouit.capstone.api.PostRequestDto
import com.kmouit.capstone.domain.jpa.*
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.exception.DuplicateFavoriteException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.jpa.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.NoSuchElementException
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max


@Service
@Transactional(readOnly = true)
class PostService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
    private val noticeService: NoticeService,
    private val boardMarkInfoRepository: BoardMarkInfoRepository,
    private val uploadService: S3UploadService,
    private val s3UploadService: S3UploadService,
    private val commentRepository: CommentRepository,
    private val lectureRoomRepository: LectureRoomRepository,
    private val lecturePostRepository: LecturePostRepository,
    private val postLikeInfoRepository: PostLikeInfoRepository,
    private val postScrapInfoRepository: PostScrapInfoRepository
) {


    @Transactional
    fun createComment(requestDto: CommentRequestDto, postId: Long, userDetail: Member) {
        val member = memberRepository.findMemberAndNoticesById(userDetail.id!!)
            ?: throw (NoSuchElementException("회원을 찾을 수 없습니다."))

        val post = postRepository.findById(postId).orElseThrow {
            NoSuchElementException("게시글을 찾을 수 없습니다.")
        }
        // 🔹 parent 댓글이 있으면 찾아서 연결
        val parent: Comments? = requestDto.parentId?.let {
            commentRepository.findById(it).orElseThrow {
                NoSuchElementException("부모 댓글을 찾을 수 없습니다.")
            }
        }
        val comment = Comments(
            content = requestDto.content,
            createdDate = LocalDateTime.now(),
            member = member,
            post = post,
            parent = parent,
            likeCount = 0
        )

        // 🔹 부모가 없으면 최상위 댓글이므로 post에 직접 연결
        if (parent == null) {
            post.comments.add(comment)
        } else {
            parent.replies.add(comment) // 생략해도 Cascade로 반영되긴 함
        }

        commentRepository.save(comment)
        // 본인이 쓴 글이면 알림 생성 생략
        if (post.member?.id == member.id) return
        noticeService.createCommentNotice(post, member, requestDto.targetUrl)
    }


    @Transactional
    fun createPost(requestDto: PostRequestDto, member: Member) {
        val createBy = memberRepository.findById(member.id!!).getOrNull()
            ?: throw NoSuchElementException("멤버를 찾을 수 없습니다.")

        val originalUrl = requestDto.imageUrls
        var thumbnailUrl: String? = null
        if (!originalUrl.isNullOrBlank()) {
            thumbnailUrl = uploadService.generateThumbnailFromOriginalUrl(originalUrl)
        }
        val newPost = Posts()
        newPost.createdDate = LocalDateTime.now()
        newPost.member = createBy
        newPost.boardType = BoardType.from(requestDto.boardType)
        newPost.title = requestDto.title
        newPost.content = requestDto.content
        newPost.imageUrls = requestDto.imageUrls
        newPost.price = requestDto.price
        newPost.thumbnailUrl = thumbnailUrl

        postRepository.save(newPost)
    }

    @Transactional
    fun updatePost(postId: Long, requestDto: PostRequestDto, member: Member) {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=$postId") }

        if (post.member!!.id != member.id) {
            throw IllegalAccessException("게시글 수정 권한이 없습니다.")
        }


        val originalUrl = requestDto.imageUrls
        var thumbnailUrl: String? = null
        if (!originalUrl.isNullOrBlank()) {
            thumbnailUrl = uploadService.generateThumbnailFromOriginalUrl(originalUrl)
        }
        post.title = requestDto.title
        post.content = requestDto.content
        post.imageUrls = requestDto.imageUrls
        post.price = requestDto.price
        post.boardType = BoardType.from(requestDto.boardType)
        post.thumbnailUrl = thumbnailUrl
        post.imageUrls = requestDto.imageUrls


    }

    @Transactional
    fun createLecturePost(dto: LecturePostRequestDto, member: Member) {
        val lectureRoom = lectureRoomRepository.findById(dto.lectureId)
            .orElseThrow { IllegalArgumentException("해당 강의가 존재하지 않습니다.") }

        val originalUrl = dto.imageUrls

        var thumbnailUrl: String? = null
        if (!originalUrl.isNullOrBlank()) {
            thumbnailUrl = uploadService.generateThumbnailFromOriginalUrl(originalUrl)
        }

        val post = LecturePosts(
            createdDate = LocalDateTime.now(),
            title = dto.title,
            content = dto.content,
            lecturePostType = LecturePostType.valueOf(dto.boardType),
            imageUrls = dto.imageUrls,
            member = member,
            lectureRoom = lectureRoom,
            thumbnailUrl = thumbnailUrl
        )
        lecturePostRepository.save(post)

        // 📢 교수에게 알림 전송 (본인이 글쓴 경우 제외)
        val professor = lectureRoom.createBy
        if (professor != null && professor.id != member.id) {
            noticeService.createLecturePostNotice(
                professor = professor,
                postTitle = post.title ?: "제목 없음",
                lectureRoomId = lectureRoom.id!!,
                postId = post.id!!
            )
        }
    }


    @Transactional
    fun saveCrawledNotices(
        noticeList: List<CrawledNoticeDto>,
        memberId: Long,
        boardType: BoardType
    ) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSearchMemberException(HttpStatus.NOT_FOUND, "존재하지 않는 회원") }

        val newPosts = mutableListOf<Posts>()

        for (dto in noticeList) {
            val exists = postRepository.existsByBoardTypeAndTitleAndCreatedDate(
                boardType, dto.title, dto.date.atStartOfDay()
            )
            if (exists) {
                println("⚠️ 중복된 게시글 발견, 저장 생략: ${dto.title}")
                continue
            }

            var originalUrlOnS3: String? = null
            var thumbnailUrl: String? = null

            val validImageUrl = dto.img.firstOrNull { url ->
                val lower = url.lowercase()
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
            }

            if (!validImageUrl.isNullOrBlank()) {
                try {
                    val (uploadedOriginal, uploadedThumbnail) =
                        uploadService.uploadExternalImageAndGenerateThumbnail(validImageUrl)
                    originalUrlOnS3 = uploadedOriginal
                    thumbnailUrl = uploadedThumbnail
                } catch (e: Exception) {
                    println("❌ 이미지 업로드 실패 (URL: $validImageUrl): ${e.message}")
                }
            }

            val newPost = Posts().apply {
                this.member = member
                this.boardType = boardType
                this.targetUrl = dto.url
                this.title = dto.title
                this.content = dto.content
                this.createdDate = dto.date.atStartOfDay()
                this.imageUrls = originalUrlOnS3
                this.thumbnailUrl = thumbnailUrl
            }
            newPosts.add(newPost)
        }

        // ✅ 배치 저장
        postRepository.saveAll(newPosts)
    }




    @Transactional
    fun getPostDetail(id: Long, currentUserId: Long?): PostDto {
        val post = postRepository.findPostWithDetails(id)
            ?: throw NoSuchElementException("게시글 없음")
        // 조회수 증가
        post.viewCount += 1
        // 내가 좋아요를 눌렀는지 체크
        val isLike = if (currentUserId != null) {
            postLikeInfoRepository.existsByMemberIdAndPostsId(currentUserId, id)
        } else {
            false
        }

        return post.toDto(currentUserId, isLike)
    }

    @Transactional
    fun getLecturePostDetail(id: Long, currentUserId: Long?):
            LecturePostsDto {
        val post = lecturePostRepository.findWithMemberAndLectureRoomById(id)
            ?: throw NoSuchElementException("게시글 없음")
        post.viewCount += 1
        return post.toDto(currentUserId)
    }


    fun getSummary(
        boardType: BoardType?,
        currentUserId: Long,
        pageSize: Int = 5,
        pageNumber: Int = 0,
    ): List<SimplePostDto> {
        if (boardType == null) throw IllegalArgumentException("게시판 타입이 null입니다.")
        val pageable = PageRequest.of(pageNumber, pageSize)  // ✅ 넘어온 pageSize 사용

        val posts = postRepository.findTopByBoardTypeWithMember(boardType, pageable)

        return posts.map { post ->
            val commentCount = commentRepository.countByPostId(post.id!!) // 🔹 별도 count 쿼리
            post.toSimpleDto(currentUserId, commentCount)
        }
    }


    fun getMultipleSummaries(
        boardTypes: List<BoardType>,
        userId: Long,
        pageSize: Int,
    ): List<SimplePostDto> {
        val pageable = PageRequest.of(0, pageSize * boardTypes.size)
        val posts = postRepository.findTopByBoardTypesWithMember(boardTypes, pageable)
        return posts
            .map { post ->
                val commentCount = commentRepository.countByPostId(post.id!!)
                post.toSimpleDto(userId, commentCount)
            }
            .sortedByDescending { it.createdDate }
            .take(pageSize)
    }

    fun findPostDtoByBoardType(
        currentUserId: Long,
        boardType: String,
        pageable: Pageable,
        filter: String?,
        query: String?,
    ): Page<SimplePostDto> {
        val boardEnum = BoardType.from(boardType) ?: throw IllegalArgumentException("유효하지 않은 게시판")

        val postPage = when {
            !query.isNullOrBlank() && filter == "title" ->
                postRepository.findByBoardTypeAndTitleContainingWithMember(boardEnum, query, pageable)

            !query.isNullOrBlank() && filter == "writer" ->
                postRepository.findByBoardTypeAndMemberNicknameContainingWithMember(boardEnum, query, pageable)

            else ->
                postRepository.findAllByBoardTypeWithMember(boardEnum, pageable)
        }

        return postPage.map { post ->
            val commentCount = commentRepository.countByPostId(post.id!!)
            post.toSimpleDto(currentUserId, commentCount)
        }
    }


    @Transactional
    fun saveBoardMarkInfo(id: Long, boardType: String) {
        val member = memberRepository.findById(id)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        val boardTypeEnum = BoardType.from(boardType.uppercase(Locale.getDefault()))

        val exists = boardMarkInfoRepository.existsByMemberAndBoardType(member, boardTypeEnum!!)
        if (exists) {
            throw DuplicateFavoriteException("이미 등록된 즐겨찾기입니다.")
        }
        val boardMarkInfo = BoardMarkInfo().apply {
            this.member = member
            this.boardType = boardTypeEnum
            this.targetUrl = "/main/community/${boardType.lowercase()}" // 필요한 경우 자동 생성
        }

        boardMarkInfoRepository.save(boardMarkInfo)
    }


    fun findMyFavorites(memberId: Long): List<BoardMarkInfoDto> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        return boardMarkInfoRepository.findAllByMemberIdWithFetch(member.id!!)
            .map { it.toDto() }
    }

    @Transactional
    fun deleteBoardMarkInfo(memberId: Long, boardType: String) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        val boardTypeEnum = BoardType.from(boardType.uppercase(Locale.ENGLISH))

        val boardMarkInfo = boardMarkInfoRepository
            .findByMemberAndBoardType(member, boardTypeEnum!!)
            ?: throw NoSuchElementException("해당 즐겨찾기를 찾을 수 없습니다.")

        boardMarkInfoRepository.delete(boardMarkInfo)
    }

    fun checkBoardMark(boardType: String, id: Long): Boolean {
        val member = memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        return boardMarkInfoRepository.existsByMemberAndBoardType(
            member,
            BoardType.from(boardType.uppercase(Locale.getDefault()))!!
        )
    }

    @Transactional
    fun deletePost(postId: Long, currentUser: Member) {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("게시글이 존재하지 않습니다.") }

        if (post.member?.id != currentUser.id) {
            throw CustomAccessDeniedException("본인의 게시글만 삭제할 수 있습니다.")
        }
        postLikeInfoRepository.deleteByPostId(postId)
        // ✅ 이미지 및 썸네일 S3 삭제
        s3UploadService.deleteAllImages(post.imageUrls, post.thumbnailUrl)
        // ✅ 게시글 DB 삭제
        postRepository.delete(post)
    }


    //****************************좋아요 기능 *******************************
    fun findMyLikes(memberId: Long): List<SimplePostDto> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        val likedPosts = postLikeInfoRepository.findAllByMember(member)
            .map { it.posts }  // Post 객체만 추출

        return likedPosts.map { post ->
            val commentCount = commentRepository.countByPostId(post!!.id!!)
            post.toSimpleDto(memberId, commentCount)
        }
    }

    @Transactional
    fun likePost(postId: Long, memberId: Long): Int {
        val post = postRepository.findById(postId).orElseThrow { NoSuchElementException("존재하지 않는 게시글") }
        val member = memberRepository.findById(memberId).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        val currentCount = ++post.likeCount
        val postLikeInfo = PostLikeInfo().apply {
            this.member = member
            this.posts = post
        }
        postLikeInfoRepository.save(postLikeInfo)
        return currentCount
    }

    @Transactional
    fun unlikePost(postId: Long, memberId: Long): Int {
        val post = postRepository.findById(postId)
            .orElseThrow { NoSuchElementException("존재하지 않는 게시글") }
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        post.likeCount = max(post.likeCount - 1, 0)
        val currentCount = post.likeCount

        val likeInfo = postLikeInfoRepository.findByMemberAndPosts(member, post)
            ?: throw NoSuchElementException("좋아요 정보가 없습니다.")

        postLikeInfoRepository.delete(likeInfo)
        return currentCount
    }

    //************************************************************************
    //*********************************스크랩 기능******************************
    fun findMyScraps(memberId: Long): List<SimplePostDto> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        val likedPosts = postScrapInfoRepository.findAllByMember(member)
            .map { it.posts }  // Post 객체만 추출

        return likedPosts.map { post ->
            val commentCount = commentRepository.countByPostId(post!!.id!!)
            post.toSimpleDto(memberId, commentCount)
        }
    }

    @Transactional
    fun scrapPost(postId: Long, memberId: Long) {
        val post = postRepository.findById(postId).orElseThrow { NoSuchElementException("존재하지 않는 게시글") }
        val member = memberRepository.findById(memberId).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        val postScrapInfo = PostScrapInfo().apply {
            this.member = member
            this.posts = post
        }
        postScrapInfoRepository.save(postScrapInfo)
    }

    @Transactional
    fun unScrapPost(postId: Long, memberId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { NoSuchElementException("존재하지 않는 게시글") }
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원") }

        val scrapInfo = postScrapInfoRepository.findByMemberAndPosts(member, post)
            ?: throw NoSuchElementException("스크랩 정보가 없습니다.")
        postScrapInfoRepository.delete(scrapInfo)
    }

    //*****************************************************************************


    fun getMyPosts(member: Member, page: Int, size: Int): Page<SimplePostDto> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"))
        return postRepository.findByMember(member, pageable)
            .map { post ->
                post.toSimpleDto(member.id, null)
            }
    }

    fun getMyComments(member: Member, page: Int, size: Int): Page<CommentsWithPostDto> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"))
        val commentsPage = commentRepository.findByMember(member, pageable)
        return commentsPage.map { it.toCommentsWithPostDto(member.id, isAnonymous = false) }
    }

    @Transactional(readOnly = true)
    fun findTodayPosts(): List<SimplePostDto> {
        val today = LocalDate.now()
        val start = today.atStartOfDay()
        val end = today.plusDays(1).atStartOfDay()

        val posts = postRepository.findByCreatedDateBetween(start, end)

        return posts.map { post ->
            val commentCount = commentRepository.countByPostId(post.id!!)
            post.toSimpleDto(currentUserId = null, commentCount = commentCount)
        }
    }




}

