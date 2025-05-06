package com.kmouit.capstone.service

import com.kmouit.capstone.MailStatus
import com.kmouit.capstone.api.DuplicateMailRoomException
import com.kmouit.capstone.domain.*
import com.kmouit.capstone.dtos.MemberSimpleDto
import com.kmouit.capstone.repository.MailRepository
import com.kmouit.capstone.repository.MailRoomInfoRepository
import com.kmouit.capstone.repository.MailRoomRepository
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class MailService(
    val mailRepository: MailRepository,
    val mailRoomRepository: MailRoomRepository,
    val mailRoomInfoRepository: MailRoomInfoRepository,
    val memberRepository: MemberRepository
) {
    @Transactional
    fun createMailRoom(username1: String, username2: String): Long {
        val creator = memberRepository.findByUsername(username1)
            ?: throw NoSuchElementException("회원을 찾을 수 없습니다.")
        val invited = memberRepository.findByUsername(username2)
            ?: throw NoSuchElementException("회원을 찾을 수 없습니다.")

        val existingRoomId = mailRoomInfoRepository.findRoomIdByTwoMembers(creator.id!!, invited.id!!)
        if (existingRoomId != null) {
            throw DuplicateMailRoomException("이미 중복된 객체가 있습니다.")
        }

        val mailRoom = mailRoomRepository.save(MailRoom()) // save 후 id가 생김
        val mailRoomInfo1 = MailRoomInfo(
            id = MailRoomInfoId(mailRoom.id!!, creator.id!!),
            mailRoom = mailRoom,
            member = creator
        )

        val mailRoomInfo2 = MailRoomInfo(
            id = MailRoomInfoId(mailRoom.id!!, invited.id!!),
            mailRoom = mailRoom,
            member = invited
        )

        mailRoom.mailRoomInfos.add(mailRoomInfo1)
        mailRoom.mailRoomInfos.add(mailRoomInfo2)

        return mailRoom.id!!
    }

    //Todo
    @Transactional
    fun createMail(sender: Member, receiver: Member, content: String) {
        val mail = Mail().apply {
            this.sender = sender
            this.receiver = receiver
            this.content = content
            this.status = MailStatus.NEW
            this.date = LocalDateTime.now()
        }
        mailRepository.save(mail)
    }

    /**
     * 내가 참여한 채팅방 찾기
     */
    fun getMyRooms(memberId: Long): List<RoomDto> {
        val roomIds = mailRoomInfoRepository.findMailRoomIdsByMemberId(memberId)

        if (roomIds.isEmpty()) return emptyList()
        val mailRooms = mailRoomRepository.findAllWithMembersByRoomIds(roomIds)

        return mailRooms.map { room ->
            val partner = room.mailRoomInfos
                .map { it.member }
                .firstOrNull { it.id != memberId }
                ?: throw NoSuchElementException("상대방 정보를 찾을 수 없습니다.")

            RoomDto(
                roomId = room.id!!,
                partner = MemberSimpleDto(partner)
            )
        }
    }


    /**
     * 채팅방 삭제
     */
    @Transactional
    fun exitMailRoom(id: Long) {
        val exists = mailRoomRepository.existsById(id)
        if (!exists) {
            throw IllegalArgumentException("해당 채팅방이 존재하지 않습니다. id=$id")
        }

        try {
            mailRoomRepository.deleteById(id)
        } catch (e: Exception) {
            throw RuntimeException("Room 삭제 도중 오류 발생", e)
        }
    }


}

data class RoomDto(
    val roomId: Long,
    val partner :MemberSimpleDto
)