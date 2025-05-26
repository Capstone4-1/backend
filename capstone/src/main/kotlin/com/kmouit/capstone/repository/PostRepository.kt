package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Posts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PostRepository : JpaRepository <Posts, Long> {

}