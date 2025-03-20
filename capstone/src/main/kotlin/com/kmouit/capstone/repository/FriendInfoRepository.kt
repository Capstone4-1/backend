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
     */
    @Query("""
        SELECT f 
        FROM FriendInfo f
        JOIN f.friendInfoId.receiveMember m
        WHERE f.friendInfoId.receiveMember.id = :receivedId AND f.status = 'SENDING'
    """)
    fun findReceivedFriendInfoById(receivedId :Long) : List<FriendInfo>
}