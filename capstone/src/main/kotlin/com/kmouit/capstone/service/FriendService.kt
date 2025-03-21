package com.kmouit.capstone.service

import com.kmouit.capstone.domain.FriendInfo
import com.kmouit.capstone.domain.FriendInfoId
import com.kmouit.capstone.domain.FriendStatus
import com.kmouit.capstone.dtos.MemberDto
import com.kmouit.capstone.repository.FriendInfoRepository
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class FriendService(
    private val friendInfoRepository: FriendInfoRepository,
    private val memberRepository: MemberRepository,

    ) {

    /**
     * 친구요청 거절
     */
    @Transactional
    fun declineFriendRequest(receiveId: Long, idToDecline: Long) {
        val friendInfo =
            friendInfoRepository.findFriendInfoBySendMemberIdAndReceiveMemberId(
                sendMemberId = idToDecline,
                receiveMemberId = receiveId
            )
                ?: throw IllegalStateException("친구요청 거절 실패")
        friendInfoRepository.delete(friendInfo)
    }


    /**
     * 친구요청 수락
     * id:요청받음
     * idToAccept:요청함
     */
    @Transactional
    fun acceptFriendRequest(id: Long, idToAccept: Long) {
        try {
            val friendInfo =
                friendInfoRepository.findFriendInfoBySendMemberIdAndReceiveMemberId(
                    sendMemberId = idToAccept,
                    receiveMemberId = id
                )
                    ?: throw IllegalStateException("친구요청 수락 실패")
            //반대방향도 추가
            addOppositeFriendInfo(id, idToAccept)
            friendInfo.status = FriendStatus.ACCEPTED
        } catch (e: Exception) {
            throw e
        }
    }

    fun addOppositeFriendInfo(id: Long, idToAccept: Long) {
        val sendMember = memberRepository.findById(id).orElseThrow()
        val receiveMember = memberRepository.findById(idToAccept).orElseThrow()
        val oppositeFriendInfo =
            FriendInfo(FriendInfoId(sendMember = sendMember, receiveMember = receiveMember), FriendStatus.ACCEPTED)
        friendInfoRepository.save(oppositeFriendInfo)
    }


    /**
     * 받은 친구요청 목록 조회
     * List<FriendInfo> -> List<MemberDto> 변환
     */
    fun findRequestMembers(id: Long): List<MemberDto> {
        val friendInfoList = friendInfoRepository.findRequestFriendInfoById(id)
        return friendInfoList.map { friendInfo -> MemberDto(friendInfo.friendInfoId!!.sendMember) }
    }


    /**
     * 친구 목록 조회
     * List<FriendInfo> -> List<MemberDto> 변환
     */
    fun findAcceptMembers(id: Long): List<MemberDto> {
        val friendInfoList = friendInfoRepository.findFriendInfoById(id)
        for (friendInfo in friendInfoList) {
            println("friendInfo = ${friendInfo}")
        }
        return friendInfoList.map { friendInfo ->  MemberDto(friendInfo.friendInfoId!!.receiveMember) }
    }


    /**
     * 친구 신청
     */
    @Transactional
    fun addFriend(id: Long, studentId: String) {
        val sender = memberRepository.findById(id).orElseThrow { IllegalStateException("회원을 찾을수 없습니다") }
        val receiver = memberRepository.findByUsername(studentId) ?: throw IllegalStateException("회원을 찾을수 없습니다")
        val existingFriendInfo = friendInfoRepository.findById(FriendInfoId(sender, receiver))
        if (existingFriendInfo.isPresent) {
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