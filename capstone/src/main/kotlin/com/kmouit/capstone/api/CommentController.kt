package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.CommentService
import com.kmouit.capstone.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("api/comment")
class CommentController(
    private val commentService: CommentService
) {

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        commentService.deleteComment(commentId, userDetails.member)
        return ResponseEntity.ok(mapOf("message" to "댓글 삭제 성공"))
    }

}