package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.FriendInfo
import com.kmouit.capstone.domain.FriendInfoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface FriendInfoRepository : JpaRepository<FriendInfo, FriendInfoId> {
    /**
     * 받은 친구 요청 조회하기
     * 패치조인으로 1+ n 문제 해결
     */
    @Query(
        """  
        select f
        from FriendInfo f
        join fetch f.friendInfoId.receiveMember rm
        join fetch f.friendInfoId.sendMember sm 
        where rm.id = :receivedId 
        and f.status = 'SENDING'
    """
    )
    fun findRequestFriendInfoById(receivedId: Long): List<FriendInfo>


    /**
     * 친구 조회하기
     */
    @Query(
        """  
        select f
        from FriendInfo f
        join fetch f.friendInfoId.receiveMember rm
        join fetch f.friendInfoId.sendMember sm 
        where sm.id = :id 
        and f.status = 'ACCEPTED'
    """
    )
    fun findFriendInfoById(id: Long): List<FriendInfo>

    @Query(
        "select f from FriendInfo f " +
                "where f.friendInfoId.sendMember.id = :sendMemberId " +
                "and f.friendInfoId.receiveMember.id = :receiveMemberId"
    )
    fun findFriendInfoBySendMemberIdAndReceiveMemberId(sendMemberId: Long, receiveMemberId: Long): FriendInfo?

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM FriendInfo f
        WHERE 
            (f.friendInfoId.sendMember.id = :userId1 AND f.friendInfoId.receiveMember.id = :userId2) OR
            (f.friendInfoId.sendMember.id = :userId2 AND f.friendInfoId.receiveMember.id = :userId1)
        AND f.status = 'ACCEPTED'
    """)
    fun areFriends(
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long
    ): Boolean


}
