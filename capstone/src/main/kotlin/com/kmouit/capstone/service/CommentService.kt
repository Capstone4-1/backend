package com.kmouit.capstone.service

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.exception.CustomAccessDeniedException
import com.kmouit.capstone.repository.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CommentService(
    private val commentRepository: CommentRepository
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
}