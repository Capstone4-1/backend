package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.api.CrawledNoticeDto
import com.kmouit.capstone.api.LecturePostRequestDto
import com.kmouit.capstone.api.PostRequestDto
import com.kmouit.capstone.domain.*
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.exception.DuplicateFavoriteException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.PrivateKey
import java.time.LocalDateTime
import java.util.*
import kotlin.NoSuchElementException
import kotlin.jvm.optionals.getOrNull


@Service
@Transactional(readOnly = true)
class PostService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
    private val noticeService: NoticeService,
    private val boardMarkInfoRepository: BoardMarkInfoRepository,
    private val uploadService: S3UploadService,
    private val commentRepository: CommentRepository,
    private val lectureRoomRepository: LectureRoomRepository,
    private val lecturePostRepository: LecturePostRepository
) {
    @Transactional
    fun createComment(requestDto: CommentRequestDto, postId: Long, userDetail: Member) {

        val member = memberRepository.findMemberAndNoticesById(userDetail.id!!)
            ?: throw (NoSuchElementException("회원을 찾을 수 없습니다."))
        val post = postRepository.findById(postId).orElseThrow {
            NoSuchElementException("게시글을 찾을 수 없습니다.")
        }
        val comment = Comments(
            content = requestDto.content,
            createdDate = LocalDateTime.now(),
            member = member,
            post = post,
            likeCount = 0
        )

        post.comments.add(comment)

        if (post.member!!.id == member.id) {
            return
        }
        noticeService.createCommentNotice(
            post, member
        )
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
    fun createLecturePost(dto: LecturePostRequestDto, member: Member) {
        // 예시 구현
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
    }

    @Transactional
    fun saveCrawledNotices(noticeList: List<CrawledNoticeDto>, memberId: Long) {
        val member = memberRepository.findById(memberId).orElseThrow { NoSearchMemberException(HttpStatus.NOT_FOUND, "존재하지 않는 회원") }
        for (crawledNoticeDto in noticeList) {
            var originalUrlOnS3: String? = null
            var thumbnailUrl: String? = null

            val validImageUrl = crawledNoticeDto.img.firstOrNull { url ->
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
                this.boardType = BoardType.NOTICE_C
                this.targetUrl = crawledNoticeDto.url
                this.title = crawledNoticeDto.title
                this.content = crawledNoticeDto.content
                this.createdDate = crawledNoticeDto.date.atStartOfDay()
                this.imageUrls = originalUrlOnS3
                this.thumbnailUrl = thumbnailUrl
            }

            postRepository.save(newPost)
        }
    }



    @Transactional
    fun getPostDetail(id: Long, currentUserId: Long?): PostDto {
        val post = postRepository.findPostWithDetails(id)
            ?: throw NoSuchElementException("게시글 없음")

        post.viewCount += 1
        return post.toDto(currentUserId)
    }



    fun getSummary(boardType: BoardType?, currentUserId: Long): List<SimplePostDto> {
        if (boardType == null) throw IllegalArgumentException("게시판 타입이 null입니다.")
        val pageable = PageRequest.of(0, 3)

        val posts = postRepository.findTopByBoardTypeWithMember(boardType, pageable)

        return posts.map { post ->
            val commentCount = commentRepository.countByPostId(post.id!!) // 🔹 별도 count 쿼리 (1번)
            post.toSimpleDto(currentUserId, commentCount)
        }
    }

    fun findPostDtoByBoardType(
        boardType: String,
        pageable: Pageable,
        filter: String?,
        query: String?
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
            post.toSimpleDto(0, commentCount)
        }
    }


    @Transactional
    fun saveBoardMarkInfo(id: Long, boardType: String, ) {
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
        return boardMarkInfoRepository.existsByMemberAndBoardType(member, BoardType.from(boardType.uppercase(Locale.getDefault()))!!)
    }

    @Transactional
    fun deletePost(postId: Long, currentUser: Member) {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("게시글이 존재하지 않습니다.") }

        if (post.member?.id != currentUser.id) {
            throw CustomAccessDeniedException("본인의 게시글만 삭제할 수 있습니다.")
        }

        postRepository.delete(post)
    }




}

