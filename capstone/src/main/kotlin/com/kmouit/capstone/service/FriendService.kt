package com.kmouit.capstone.service

import com.kmouit.capstone.domain.FriendInfo
import com.kmouit.capstone.domain.FriendInfoId
import com.kmouit.capstone.domain.FriendStatus
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.dtos.MemberSimpleDto
import com.kmouit.capstone.repository.FriendInfoRepository
import com.kmouit.capstone.repository.MemberRepository
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
        noticeService.createNotice(sendMember, "${receivedMember.name}님이 친구요청을 거절하셨습니다!")
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
            noticeService.createNotice(sendMember, "${receivedMember.name}님이 친구요청을 수락하셨습니다!")
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
        for (friendInfo in friendInfoList) {
            println("friendInfo = $friendInfo")
        }
        return friendInfoList.map { friendInfo -> MemberSimpleDto(friendInfo.friendInfoId!!.receiveMember) }
    }


    /**
     * 친구 신청
     */
    @Transactional
    fun addFriend(id: Long, studentId: String) {
        val sender = memberRepository.findById(id).orElseThrow { NoSuchElementException("송신 회원을 찾을수 없습니다") }
        val receiver = memberRepository.findByUsername(studentId) ?: throw NoSuchElementException("수신 회원을 찾을수 없습니다")
        if (sender.id == receiver.id) {
            throw IllegalStateException("자신에게 요청을 보낼 수 없습니다")
        }
        val existingFriendInfo = friendInfoRepository.findById(FriendInfoId(sender, receiver))
        if (existingFriendInfo.isPresent) {
            throw IllegalStateException("이미 친구 요청을 보냈거나 친구 상태입니다")
        }
        val newFriendInfo = FriendInfo(
            friendInfoId = FriendInfoId(sender, receiver),
            status = FriendStatus.SENDING
        )
        friendInfoRepository.save(newFriendInfo)
        noticeService.createNotice(receiver, "${sender.name}님이 친구 요청을 보냈습니다!")
    }

    fun deleteFriend() {

    }


}