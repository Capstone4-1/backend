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
    @Query("""  
        from FriendInfo f
        join fetch f.friendInfoId.receiveMember rm
        join fetch f.friendInfoId.sendMember sm 
        where rm.id = :receivedId 
        and f.status = 'SENDING'
    """)
    fun findReceivedFriendInfoById(receivedId :Long) : List<FriendInfo>
}