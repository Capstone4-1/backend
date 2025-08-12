package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.LectureMarkInfo
import com.kmouit.capstone.domain.jpa.LectureRoom
import com.kmouit.capstone.domain.jpa.Member
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
    SELECT DISTINCT lmi FROM LectureMarkInfo lmi
    JOIN FETCH lmi.lectureRoom lr
    JOIN FETCH lr.createBy
    LEFT JOIN FETCH lr.schedules s
    WHERE lmi.member = :member
    """
    )
    fun findWithLectureRoomAndProfessorByMember(
        @Param("member") member: Member,
    ): List<LectureMarkInfo>
    fun countByLectureRoom_Id(roomId: Long): Int

    fun deleteAllByMember(member: Member)

}