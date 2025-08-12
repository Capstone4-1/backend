package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.domain.jpa.LecturePosts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface LecturePostRepository : JpaRepository<LecturePosts, Long> {

    @Query(
        """
    SELECT lp FROM LecturePosts lp
    JOIN FETCH lp.member m
    JOIN FETCH lp.lectureRoom lr
    WHERE lr.id = :lectureRoomId AND lp.lecturePostType = :postType
    ORDER BY lp.createdDate DESC
    """
    )
    fun findWithMemberAndLectureRoomByLectureRoomIdAndLecturePostType(
        @Param("lectureRoomId") lectureRoomId: Long,
        @Param("postType") postType: LecturePostType,
    ): List<LecturePosts>


    @Query(
        """
        SELECT lp FROM LecturePosts lp
        JOIN FETCH lp.member
        JOIN FETCH lp.lectureRoom
        WHERE lp.id = :id
        """
    )
    fun findWithMemberAndLectureRoomById(@Param("id") id: Long): LecturePosts?
}