package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.LectureMarkInfo
import com.kmouit.capstone.domain.LectureRoom
import com.kmouit.capstone.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface LectureRoomRepository :JpaRepository<LectureRoom, Long> {

}