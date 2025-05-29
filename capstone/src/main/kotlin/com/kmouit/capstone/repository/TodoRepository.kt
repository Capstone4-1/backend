package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Todo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TodoRepository : JpaRepository<Todo, Long> {
    fun findByMemberId(memberId: Long): List<Todo>
}