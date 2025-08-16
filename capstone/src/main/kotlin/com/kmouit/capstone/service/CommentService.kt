package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.api.CommentRequestDto
import com.kmouit.capstone.domain.jpa.*
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.repository.jpa.CommentRepository
import com.kmouit.capstone.repository.jpa.LecturePostRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
import com.kmouit.capstone.repository.jpa.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
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
        val post = postRepository.findById(postId)
            .orElseThrow { NoSuchElementException("게시글 존재 x") }

        val isSecret = post.boardType == BoardType.SECRET
        val comments = commentRepository.findTopLevelCommentsByPostId(postId)

        return comments
            .map { parent ->
                val childCount = commentRepository.countByParent_Id(parent.id!!)
                parent.toDto(currentUserId, isSecret).apply {
                    countChildren = childCount.toInt()
                }
            }
            .sortedByDescending { it.createdDate }
    }


    fun getTopLevelCommentsForLecturePost(lecturePostId: Long, currentUserId: Long?): List<CommentDto> {
        val lecturePost = lecturePostRepository.findById(lecturePostId)
            .orElseThrow { NoSuchElementException("해당 강의 게시글이 존재하지 않습니다.") }

        return lecturePost.comments
            .filter { it.parent == null }
            .sortedByDescending { it.createdDate }
            .map { it.toDto(currentUserId, false) }
    }

    fun getReplies(parentCommentId: Long, currentUserId: Long?): List<CommentDto> {
        val parentComment =
            commentRepository.findById(parentCommentId).orElseThrow { NoSuchElementException("존재하지 않는 댓글") }

        var isSecret = false
        if (parentComment.post!!.boardType== BoardType.SECRET){
            isSecret = true
        }
        val replies = commentRepository.findRepliesWithMemberByParentId(parentCommentId)

        return replies // children: List<Comments>
            .sortedBy { it.createdDate }
            .map { it.toDto(currentUserId, isSecret) }
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

    fun findTodayComments(): List<CommentsWithPostDto> {
        val today = LocalDate.now()
        val start = today.atStartOfDay()
        val end = today.plusDays(1).atStartOfDay()
        val comments = commentRepository.findByCreatedDateBetween(start, end)

        return comments.map { comment ->
            comment.toCommentsWithPostDto(currentUserId = null, isAnonymous = false)
        }
    }


}