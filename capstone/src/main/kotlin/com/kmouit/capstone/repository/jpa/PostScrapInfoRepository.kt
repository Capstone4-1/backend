package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.domain.jpa.PostScrapInfo
import com.kmouit.capstone.domain.jpa.Posts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface PostScrapInfoRepository : JpaRepository<PostScrapInfo, Long> {
    fun findByMemberAndPosts(member: Member, posts: Posts): PostScrapInfo?
    fun findAllByMember(member: Member): List<PostScrapInfo>
}