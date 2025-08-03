package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.PostLikeInfo
import com.kmouit.capstone.domain.Posts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PostLikeInfoRepository : JpaRepository <PostLikeInfo, Long> {
    fun findByMemberAndPosts(member: Member, posts: Posts): PostLikeInfo?
    fun existsByMemberIdAndPostsId(currentUserId: Long, id: Long): Boolean
}
