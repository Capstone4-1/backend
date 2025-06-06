package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.api.CrawledNoticeDto
import com.kmouit.capstone.api.PostRequestDto
import com.kmouit.capstone.domain.*
import com.kmouit.capstone.exception.DuplicateFavoriteException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.BoardMarkInfoRepository
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val uploadService: S3UploadService
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

    fun getSummary(boardType: BoardType?, id: Long): List<SimplePostDto> {
        if (boardType == null) throw IllegalArgumentException("게시판 타입이 null입니다.")

        val pageable = PageRequest.of(0, 3)
        val posts = postRepository.findTopByBoardTypeWithMember(boardType, pageable)

        return posts.map { it.toSimpleDto(id) }
    }

    fun findPostDtoByBoardType(boardType: String, pageable: Pageable): Page<SimplePostDto> {
        val postPage = postRepository.findAllByBoardTypeWithMember(
            BoardType.from(boardType)!!, pageable
        )
        return postPage.map { it.toSimpleDto(0) } //수정
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

}

