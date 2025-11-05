package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.domain.jpa.PostScrapInfo
import com.kmouit.capstone.domain.jpa.Posts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PostScrapInfoRepository : JpaRepository<PostScrapInfo, Long> {
    fun findByMemberAndPosts(member: Member, posts: Posts): PostScrapInfo?
    fun findAllByMember(member: Member): List<PostScrapInfo>
    fun existsByMemberIdAndPostsId(memberId: Long, postsId: Long): Boolean
    @Modifying(clearAutomatically = true)
    @Query("delete from PostScrapInfo ps where ps.posts.id = :postId")
    fun deleteByPostId(@Param("postId") postId: Long)
}
