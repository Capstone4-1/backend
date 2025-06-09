package com.kmouit.capstone.domain

import jakarta.persistence.*
import net.coobird.thumbnailator.Thumbnails
import java.time.LocalDate

@Entity
class LectureRoom (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    var title : String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var createBy :Member? = null,
    val grade : Int? = null,
    val semester : Int? = null,
    val themeColor : String? = null,
    val intro : String? = null,
    var createdDate : LocalDate? = null,
    var code : String? = null,
)

data class LectureRoomDto(
    val id: Long,
    val title: String,
    val professorName: String,
    val professorNickname: String,
    val professorId: Long?,
    val professorThumbnail : String?,
    val themeColor: String,
    val grade: Int?,
    val semester: Int?,
    val intro: String?,
    val createdDate: LocalDate?,
    val code: String?,
    val isMarked: Boolean
) {
    companion object {
        fun from(room: LectureRoom, isMarked: Boolean = false): LectureRoomDto {
            return LectureRoomDto(
                id = room.id!!,
                title = room.title ?: "",
                professorName = room.createBy?.name ?: "미정",
                professorNickname = room.createBy?.nickname!!,
                professorId = room.createBy?.id,
                professorThumbnail = room.createBy?.thumbnailUrl,
                themeColor = room.themeColor ?: "#000000",
                grade = room.grade,
                semester = room.semester,
                intro = room.intro,
                createdDate = room.createdDate,
                code = room.code,
                isMarked = isMarked
            )
        }
    }
}

data class LectureRoomSummaryDto(
    val id: Long,
    val title: String,
    val professorId: Long?,
    val professorName: String,
    val professorNickname: String,
    val grade: Int?,
    val semester: Int?,
    val intro: String?,
    val themeColor: String,
    val createdDate: LocalDate?,
    val markedCount: Int? // ✅ 추가됨
) {
    companion object {
        fun from(room: LectureRoom, markedCount: Int =0): LectureRoomSummaryDto {
            return LectureRoomSummaryDto(
                id = room.id!!,
                title = room.title ?: "",
                professorId = room.createBy?.id,
                professorName = room.createBy?.name ?: "미정",
                professorNickname = room.createBy?.nickname ?: "미정",
                grade = room.grade,
                semester = room.semester,
                intro = room.intro,
                themeColor = room.themeColor ?: "#000000",
                createdDate = room.createdDate,
                markedCount = markedCount // ✅ 반영
            )
        }
    }
}