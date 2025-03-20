package com.kmouit.capstone.service

import com.kmouit.capstone.domain.FriendInfo
import com.kmouit.capstone.domain.FriendInfoId
import com.kmouit.capstone.domain.FriendStatus
import com.kmouit.capstone.dtos.RequestMemberDto
import com.kmouit.capstone.repository.FriendInfoRepository
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull


@Service
class FriendService(
    private val friendInfoRepository: FriendInfoRepository,
    private val memberRepository: MemberRepository,

    ) {

    fun findRequestMembers(id: Long): List<RequestMemberDto> {
        val friendInfoList = friendInfoRepository.findReceivedFriendInfoById(id)
        return friendInfoList.map { friendInfo ->  RequestMemberDto(friendInfo.friendInfoId!!.sendMember)}
    }
    fun addFriend(id: Long, studentId: String) {
        println("id = ${id}")
        println("studentId = ${studentId}")

        val sender = memberRepository.findById(id).getOrNull() ?: throw IllegalStateException("회원을 찾을수 없습니다")
        println("디버그")
        val receiver = memberRepository.findByUsername(studentId) ?: throw IllegalStateException("회원을 찾을수 없습니다")
        val existingFriendInfo = friendInfoRepository.findById(FriendInfoId(sender, receiver))

        if(existingFriendInfo.isPresent){
            throw IllegalStateException("이미 친구 요청을 보냈거나 친구 상태입니다")
        }

        val newFriendInfo = FriendInfo(
            friendInfoId = FriendInfoId(sender, receiver),
            status = FriendStatus.SENDING
        )
        friendInfoRepository.save(newFriendInfo)
    }

    fun setStatusToAccept() {

    }

    fun deleteFriend() {

    }




}