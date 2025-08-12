package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.LectureRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface LectureRoomRepository :JpaRepository<LectureRoom, Long> {

}