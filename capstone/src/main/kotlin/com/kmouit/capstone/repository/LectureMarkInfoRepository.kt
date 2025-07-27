package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.LectureMarkInfo
import com.kmouit.capstone.domain.LectureRoom
import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface LectureMarkInfoRepository : JpaRepository<LectureMarkInfo, Long> {
    fun findByMemberAndLectureRoom(member: Member, room: LectureRoom): LectureMarkInfo?
    fun existsByMemberAndLectureRoom(member: Member, room: LectureRoom): Boolean

    @Query(
        """
    SELECT lmi FROM LectureMarkInfo lmi
    JOIN FETCH lmi.lectureRoom lr
    JOIN FETCH lr.createBy
    WHERE lmi.member = :member
"""
    )
    fun findWithLectureRoomAndProfessorByMember(
        @Param("member") member: Member,
    ): List<LectureMarkInfo>

    fun countByLectureRoom_Id(roomId: Long): Int

    fun deleteAllByMember(member: Member)

}