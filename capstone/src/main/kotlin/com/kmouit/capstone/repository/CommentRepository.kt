package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Comments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface CommentRepository : JpaRepository<Comments, Long> {
    fun countByPostId(id: Long): Long

    @Query("SELECT c.post.id AS postId, COUNT(c.id) AS cnt FROM Comments c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    fun countByPostIds(@Param("postIds") postIds: List<Long>): Map<Long, Long>

}