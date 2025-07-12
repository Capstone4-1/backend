package com.kmouit.capstone.repository

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.Comments
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

    @Query(
        """
    SELECT p FROM Posts p
    JOIN FETCH p.member m
    WHERE p.boardType = :boardType
      AND p.title LIKE %:title%
    """
    )
    fun findByBoardTypeAndTitleContainingWithMember(
        @Param("boardType") boardType: BoardType,
        @Param("title") title: String,
        pageable: Pageable,
    ): Page<Posts>

    @Query(
        """
    SELECT p FROM Posts p
    JOIN FETCH p.member m
    WHERE p.boardType = :boardType
      AND m.nickname LIKE %:nickname%
    """
    )
    fun findByBoardTypeAndMemberNicknameContainingWithMember(
        @Param("boardType") boardType: BoardType,
        @Param("nickname") nickname: String,
        pageable: Pageable,
    ): Page<Posts>


    @Query(
        """
    SELECT c FROM Comments c 
    JOIN FETCH c.member
    WHERE c.post.id = :postId AND c.parent IS NULL
"""
    )
    fun findTopLevelCommentsByPostId(@Param("postId") postId: Long): List<Comments>

    @Query("""
    SELECT p FROM Posts p
    JOIN FETCH p.member
    WHERE p.boardType IN :boardTypes
    ORDER BY p.createdDate DESC
""")
    fun findTopByBoardTypesWithMember(
        @Param("boardTypes") boardTypes: List<BoardType>,
        pageable: Pageable
    ): List<Posts>
}