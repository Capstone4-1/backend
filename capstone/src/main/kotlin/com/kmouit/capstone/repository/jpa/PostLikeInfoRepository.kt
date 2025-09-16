package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.domain.jpa.PostLikeInfo
import com.kmouit.capstone.domain.jpa.Posts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PostLikeInfoRepository : JpaRepository <PostLikeInfo, Long> {
    fun findByMemberAndPosts(member: Member, posts: Posts): PostLikeInfo?
    fun existsByMemberIdAndPostsId(currentUserId: Long, id: Long): Boolean
    fun findAllByMember(member: Member): List<PostLikeInfo>
    @Modifying
    @Query("delete from PostLikeInfo p where p.posts.id = :postId")
    fun deleteByPostId(@Param("postId") postId: Long)

}
