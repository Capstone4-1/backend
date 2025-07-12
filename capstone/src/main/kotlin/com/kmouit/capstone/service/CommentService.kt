package com.kmouit.capstone.service

import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.domain.CommentDto
import com.kmouit.capstone.domain.Comments
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.toDto
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.CommentRepository
import com.kmouit.capstone.repository.LecturePostRepository
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.PostRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.LocalDateTime


@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val lecturePostRepository: LecturePostRepository,
    private val noticeService: NoticeService,
) {


    @Transactional
    fun deleteComment(commentId: Long, currentUser: Member) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { IllegalArgumentException("댓글이 존재하지 않습니다.") }
        if (comment.member!!.id != currentUser.id) {
            throw CustomAccessDeniedException("본인의 댓글만 삭제할 수 있습니다.")
        }
        commentRepository.delete(comment)
    }


    /**
     * 최상위 댓글 가져오기
     */
    fun getTopLevelComments(postId: Long, currentUserId: Long?): List<CommentDto> {
        val comments = postRepository.findTopLevelCommentsByPostId(postId)

        return comments.filter { it.parent == null }.sortedByDescending { it.createdDate }
            .map { it.toDto(currentUserId) }
    }

    fun getTopLevelCommentsForLecturePost(lecturePostId: Long, currentUserId: Long?): List<CommentDto> {
        val lecturePost = lecturePostRepository.findById(lecturePostId)
            .orElseThrow { NoSuchElementException("해당 강의 게시글이 존재하지 않습니다.") }

        return lecturePost.comments
            .filter { it.parent == null }
            .sortedByDescending { it.createdDate }
            .map { it.toDto(currentUserId) }
    }

    fun getReplies(parentCommentId: Long, currentUserId: Long?): List<CommentDto> {
        val replies = commentRepository.findRepliesWithMemberByParentId(parentCommentId)

        return replies // children: List<Comments>
            .sortedBy { it.createdDate }
            .map { it.toDto(currentUserId) }
    }

    /**
     * 강의 게시글 댓글 가져오기
     */


    @Transactional
    fun createCommentForLecturePost(lecturePostId: Long, requestDto: CommentRequestDto, userDetail: Member) {
        val member = memberRepository.findMemberAndNoticesById(userDetail.id!!)
            ?: throw NoSuchElementException("회원을 찾을 수 없습니다.")

        val lecturePost = lecturePostRepository.findById(lecturePostId).orElseThrow {
            NoSuchElementException("강의 게시글을 찾을 수 없습니다.")
        }

        val parent: Comments? = requestDto.parentId?.let {
            commentRepository.findById(it).orElseThrow {
                NoSuchElementException("부모 댓글을 찾을 수 없습니다.")
            }
        }

        val comment = Comments(
            content = requestDto.content,
            createdDate = LocalDateTime.now(),
            member = member,
            lecturePost = lecturePost,
            parent = parent,
            likeCount = 0
        )

        if (parent == null) {
            // 최상위 댓글
            lecturePost.comments.add(comment) // 양방향 관계 설정 (nullable이면 생략해도 동작 가능)
        } else {
            parent.replies.add(comment)
        }

        commentRepository.save(comment)

        // 자기 자신 글에 댓글 달면 알림 생략
        if (lecturePost.member?.id == member.id) return

        noticeService.createCommentNoticeForLecturePost(lecturePost, member)
    }


}