package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.LectureBoard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface LectureBoardRepository : JpaRepository <LectureBoard, Long> {
}