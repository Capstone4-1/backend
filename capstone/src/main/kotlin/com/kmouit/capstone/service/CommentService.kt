package com.kmouit.capstone.service

import com.kmouit.capstone.domain.CommentDto
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.toDto
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.repository.CommentRepository
import com.kmouit.capstone.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository
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

    fun getReplies(parentCommentId: Long, currentUserId: Long?): List<CommentDto> {
        val replies = commentRepository.findRepliesWithMemberByParentId(parentCommentId)

        return replies // children: List<Comments>
            .sortedBy { it.createdDate }
            .map { it.toDto(currentUserId) }
    }

}