package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.FriendInfo
import com.kmouit.capstone.domain.FriendInfoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface FriendInfoRepository : JpaRepository<FriendInfo, FriendInfoId> {
    /**
     * 받은 친구 요청 조회하기
     * 페치조인으로 1+ n 문제 해결
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


}
