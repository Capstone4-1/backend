package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.BoardType.SECRET
import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.api.CrawledNoticeDto
import com.kmouit.capstone.api.PostRequestDto
import com.kmouit.capstone.domain.*
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.NoSuchElementException
import kotlin.jvm.optionals.getOrNull


@Service
@Transactional(readOnly = true)
class PostService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
    private val noticeService: NoticeService,
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

        if(post.member!!.id == member.id){
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
        val newPost = Posts()
        newPost.createdDate = LocalDateTime.now()
        newPost.member = createBy
        newPost.boardType = BoardType.from(requestDto.boardType)
        newPost.title = requestDto.title
        newPost.content = requestDto.content
        newPost.imageUrls = requestDto.imageUrls
        newPost.price = requestDto.price


        postRepository.save(newPost)
    }

    @Transactional
    fun saveCrawledNotices(noticeList: List<CrawledNoticeDto>, member: Member) {
        for (crawledNoticeDto in noticeList) {
            val newPost = Posts()
            newPost.member = member
            newPost.boardType = BoardType.NOTICE_C
            newPost.title = crawledNoticeDto.title
            newPost.content = crawledNoticeDto.content
            // LocalDate → LocalDateTime 변환
            newPost.createdDate = crawledNoticeDto.date.atStartOfDay()
            if (crawledNoticeDto.img.isNotEmpty()) {
                newPost.imageUrls = crawledNoticeDto.img[0]
            }

            postRepository.save(newPost)
        }
    }

    fun findPostDtoByBoardType(boardType: String, pageable: Pageable): Page<SimplePostDto> {
        val postPage = postRepository.findAllByBoardTypeOrderByCreatedDateDesc(
            BoardType.from(boardType)!!, pageable
        )
        return postPage.map { it.toSimpleDto() }
    }

    @Transactional
    fun getPostDetail(id: Long, currentUserId: Long?): PostDto {
        val post = postRepository.findById(id)
            .orElseThrow { NoSuchElementException("게시글 없음") }
        post.viewCount += 1
        return post.toDto(currentUserId)
    }


}

