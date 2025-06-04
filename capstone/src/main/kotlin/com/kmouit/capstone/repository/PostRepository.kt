package com.kmouit.capstone.repository

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.Posts
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PostRepository : JpaRepository<Posts, Long> {

    fun findAllByBoardTypeOrderByCreatedDateDesc(boardType: BoardType, pageable: Pageable): Page<Posts>


    @Query(
        """
    SELECT p FROM Posts p
    JOIN FETCH p.member
    LEFT JOIN FETCH p.comments c
    LEFT JOIN FETCH c.member
    WHERE p.id = :id
"""
    )
    fun findPostWithDetails(@Param("id") id: Long): Posts?

    @Query(
        """
    SELECT p FROM Posts p
    JOIN FETCH p.member
    WHERE p.boardType = :boardType
    ORDER BY p.createdDate DESC
    """
    )
    fun findTopByBoardTypeWithMember(
        @Param("boardType") boardType: BoardType,
        pageable: Pageable,
    ): List<Posts>


    @Query(
        """
    SELECT p FROM Posts p
    JOIN FETCH p.member
    WHERE p.boardType = :boardType
    ORDER BY p.createdDate DESC
    """,
        countQuery = """
    SELECT COUNT(p) FROM Posts p
    WHERE p.boardType = :boardType
    """
    )
    fun findAllByBoardTypeWithMember(
        @Param("boardType") boardType: BoardType,
        pageable: Pageable,
    ): Page<Posts>
}