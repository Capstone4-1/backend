package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Comments
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.Posts
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface CommentRepository : JpaRepository<Comments, Long> {

    @Query("SELECT c.post.id AS postId, COUNT(c.id) AS cnt FROM Comments c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    fun countByPostIds(@Param("postIds") postIds: List<Long>): Map<Long, Long>


    @Query(
        """
    SELECT c FROM Comments c
    JOIN FETCH c.member
    WHERE c.parent.id = :parentId
    ORDER BY c.createdDate ASC
"""
    )
    fun findRepliesWithMemberByParentId(@Param("parentId") parentId: Long): List<Comments>
    fun countByParent_Id(parentId: Long): Long
    fun countByPostId(id: Long): Long


    @Query(
        """
    SELECT c FROM Comments c 
    JOIN FETCH c.member
    WHERE c.post.id = :postId AND c.parent IS NULL
"""
    )
    fun findTopLevelCommentsByPostId(@Param("postId") postId: Long): List<Comments>
    fun findByMember(member: Member, pageable: Pageable): Page<Comments>



}