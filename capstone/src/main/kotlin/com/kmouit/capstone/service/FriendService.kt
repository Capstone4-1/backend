package com.kmouit.capstone.service

import com.kmouit.capstone.domain.jpa.FriendInfo
import com.kmouit.capstone.domain.jpa.FriendInfoId
import com.kmouit.capstone.domain.jpa.FriendStatus
import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.dtos.MemberSimpleDto
import com.kmouit.capstone.repository.jpa.FriendInfoRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class FriendService(
    private val friendInfoRepository: FriendInfoRepository,
    private val memberRepository: MemberRepository,
    private val noticeService: NoticeService

) {

    /**
     * 친구요청 거절
     * (친구요청 받은이, 보낸이)
     */
    @Transactional
    fun declineFriendRequest(receiveId: Long, idToDecline: Long) {
        val sendMember = memberRepository.findById(idToDecline).orElseThrow()
        val receivedMember = memberRepository.findById(receiveId).orElseThrow()
        val friendInfo = friendInfoRepository.findById(FriendInfoId(sendMember, receivedMember)).orElseThrow { IllegalStateException("친구요청 거절 실패") }
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
            val sendMember = memberRepository.findById(idToAccept).orElseThrow()
            val receivedMember = memberRepository.findById(id).orElseThrow()

            val friendInfo =
                friendInfoRepository.findById(FriendInfoId(sendMember, receivedMember)).orElseThrow { NoSuchElementException() }
            //반대방향도 추가
            addOppositeFriendInfo(sendMember, receivedMember)
            friendInfo.status = FriendStatus.ACCEPTED
        } catch (e: NoSuchElementException) {
            throw NoSuchElementException("존재하지 않는 회원입니다")
        } catch (e: Exception) {
            throw Exception("예상치 못한 오류")
        }
    }

    fun addOppositeFriendInfo(sendMember: Member, receivedMember: Member) {
        val oppositeFriendInfo =
            FriendInfo(FriendInfoId(sendMember = receivedMember, receiveMember = sendMember), FriendStatus.ACCEPTED)
        friendInfoRepository.save(oppositeFriendInfo)
    }


    /**
     * 받은 친구요청 목록 조회
     * List<FriendInfo> -> List<MemberDto> 변환
     */
    fun findRequestMembers(id: Long): List<MemberSimpleDto> {
        val friendInfoList = friendInfoRepository.findRequestFriendInfoById(id)
        return friendInfoList.map { friendInfo -> MemberSimpleDto(friendInfo.friendInfoId!!.sendMember) }
    }


    /**
     * 친구 목록 조회
     * List<FriendInfo> -> List<MemberDto> 변환
     */
    fun findAcceptMembers(id: Long): List<MemberSimpleDto> {
        val friendInfoList = friendInfoRepository.findFriendInfoById(id)
        return friendInfoList.map { friendInfo -> MemberSimpleDto(friendInfo.friendInfoId!!.receiveMember) }
    }


    /**
     * 친구 신청
     */
    @Transactional
    fun addFriend(myId: Long, receiverId: Long) {
        val sender = memberRepository.findById(myId)
            .orElseThrow { NoSuchElementException("송신 회원을 찾을 수 없습니다") }

        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { NoSuchElementException("수신 회원을 찾을 수 없습니다") }

        if (sender.id == receiver.id)
            throw IllegalStateException("자신에게 요청을 보낼 수 없습니다")

        val friendId = FriendInfoId(sender, receiver)
        if (friendInfoRepository.existsById(friendId)) {
            throw IllegalStateException("이미 친구 요청을 보냈거나 친구 상태입니다")
        }

        friendInfoRepository.save(
            FriendInfo(friendInfoId = friendId, status = FriendStatus.SENDING)
        )
    }

    fun deleteFriend() {

    }

    @Transactional
    fun addFriendByNickname(myId: Long, nickname: String) {
        val sender = memberRepository.findById(myId)
            .orElseThrow { NoSuchElementException("송신 회원을 찾을 수 없습니다") }
        val receiver = memberRepository.findByNickname(nickname)
            ?: throw NoSuchElementException("수신 회원을 찾을 수 없습니다")
        if (sender.id == receiver.id) {
            throw IllegalStateException("자기 자신에게 친구 요청을 보낼 수 없습니다")
        }
        val friendId = FriendInfoId(sender, receiver)

        // 중복 요청 방지
        if (friendInfoRepository.existsById(friendId)) {
            throw IllegalStateException("이미 친구 요청을 보냈거나 친구 상태입니다")
        }
        val newFriendInfo = FriendInfo(
            friendInfoId = friendId,
            status = FriendStatus.SENDING
        )

        friendInfoRepository.save(newFriendInfo)
    }



    @Transactional
    fun removeFriend(currentUserId: Long, targetId: Long) {
        val sendMember = memberRepository.findById(currentUserId)
            .orElseThrow { NoSuchElementException("회원이 존재하지 않습니다") }

        val receiveMember = memberRepository.findById(targetId)
            .orElseThrow { NoSuchElementException("대상 회원이 존재하지 않습니다") }

        val friendInfoId1 = FriendInfoId(sendMember, receiveMember)
        val friendInfoId2 = FriendInfoId(receiveMember, sendMember)

        if (friendInfoRepository.existsById(friendInfoId1)) {
            friendInfoRepository.deleteById(friendInfoId1)
        }

        if (friendInfoRepository.existsById(friendInfoId2)) {
            friendInfoRepository.deleteById(friendInfoId2)
        }
    }


}