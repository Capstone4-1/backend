package com.kmouit.capstone.service

import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.Role
import com.kmouit.capstone.api.CreateLectureRoomRequest
import com.kmouit.capstone.domain.jpa.*
import com.kmouit.capstone.repository.jpa.LectureMarkInfoRepository
import com.kmouit.capstone.repository.jpa.LecturePostRepository
import com.kmouit.capstone.repository.jpa.LectureRoomRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Transactional(readOnly = true)
@Service
class LectureService(
    private val lectureRoomRepository: LectureRoomRepository,
    private val memberRepository: MemberRepository,
    private val lecturePostRepository: LecturePostRepository,
    private val lectureMarkInfoRepository: LectureMarkInfoRepository,
) {

    @Transactional
    fun createLectureRoom(userId: Long, request: CreateLectureRoomRequest): LectureRoomDto {
        val member = memberRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        if (!member.roles.contains(Role.PROFESSOR) && !member.roles.contains(Role.ADMIN)) {
            throw IllegalAccessException("강의 생성 권한이 없습니다.")
        }

        val lectureRoom = LectureRoom(
            title = request.title,
            createBy = member,
            grade = request.grade,
            semester = request.semester,
            intro = request.intro,
            themeColor = request.themeColor,
            createdDate = LocalDate.now(),
            code = generateCode()
        )

        // 저장 전 ScheduleInfo 리스트 생성 및 lectureRoom에 추가
        request.lectureTimes.forEach { time ->
            val schedule = ScheduleInfo(
                lectureRoom = lectureRoom,
                dow = time.day,
                startTime = time.start,
                endTime = time.end
            )
            lectureRoom.schedules.add(schedule)
        }
        val saved = lectureRoomRepository.save(lectureRoom)
        return LectureRoomDto.from(saved, memberId = userId)
    }


    private fun generateCode(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }



    fun getAllLectureRooms(): List<LectureRoomSummaryDto> {
        return lectureRoomRepository.findAll()
            .map { room ->
                val markedCount = lectureMarkInfoRepository.countByLectureRoom_Id(room.id!!)
                LectureRoomSummaryDto.from(room, markedCount)
            }
    }

    fun getLectureRoomById(id: Long, memberId: Long): LectureRoomDto {
        val lectureRoom = lectureRoomRepository.findById(id)
            .orElseThrow { NoSuchElementException("해당 강의실이 존재하지 않습니다.") }

        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다.") }

        val isMarked = lectureMarkInfoRepository.existsByMemberAndLectureRoom(member, lectureRoom)

        return LectureRoomDto.from(lectureRoom, isMarked, memberId)
    }

    fun getPostsByLectureAndPostType(
        lectureId: Long,
        postType: LecturePostType,
        currentUserId: Long?
    ): List<LecturePostsDto> {
        val posts = lecturePostRepository
            .findWithMemberAndLectureRoomByLectureRoomIdAndLecturePostType(lectureId, postType)

        return posts.map { it.toDto(currentUserId) }
    }


    // 강의실 즐겨찾기===
    fun findMyLectureFavorites(memberId: Long): List<LectureRoomSummaryDto> {
        val member = memberRepository.findById(memberId).orElseThrow()
        val markList = lectureMarkInfoRepository.findWithLectureRoomAndProfessorByMember(member)

        return markList
            .distinctBy { it.lectureRoom?.id } // ✅ 중복 제거
            .map { LectureRoomSummaryDto.from(it.lectureRoom!!) }
    }

    @Transactional
    fun saveLectureMark(memberId: Long, lectureRoomId: Long) {
        val member = memberRepository.findById(memberId).orElseThrow()
        val room = lectureRoomRepository.findById(lectureRoomId).orElseThrow()
        if (lectureMarkInfoRepository.existsByMemberAndLectureRoom(member, room)) return
        val mark = LectureMarkInfo(member = member, lectureRoom = room, targetUrl = null)
        lectureMarkInfoRepository.save(mark)
    }

    @Transactional

    fun deleteLectureMark(memberId: Long, lectureRoomId: Long) {
        val member = memberRepository.findById(memberId).orElseThrow()
        val room = lectureRoomRepository.findById(lectureRoomId).orElseThrow()
        val mark = lectureMarkInfoRepository.findByMemberAndLectureRoom(member, room)
            ?: throw IllegalArgumentException("즐겨찾기 정보가 존재하지 않습니다.")
        lectureMarkInfoRepository.delete(mark)
    }
    //=======================
}