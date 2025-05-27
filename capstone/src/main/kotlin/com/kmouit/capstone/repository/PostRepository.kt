package com.kmouit.capstone.repository

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.domain.Posts
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PostRepository : JpaRepository<Posts, Long> {

    fun findAllByBoardTypeOrderByCreatedDateDesc(boardType: BoardType, pageable: Pageable): Page<Posts>
}