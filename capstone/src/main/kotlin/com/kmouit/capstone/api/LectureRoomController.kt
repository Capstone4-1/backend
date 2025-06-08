package com.kmouit.capstone.api

import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.domain.LectureRoom
import com.kmouit.capstone.domain.LectureRoomDto
import com.kmouit.capstone.domain.LectureRoomSummaryDto
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.LectureService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/lecture-room")
class LectureRoomController(
    private val lectureService: LectureService,
    private val postService: LectureService
) {

    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    @PostMapping
    fun createLectureRoom(
        @RequestBody request: CreateLectureRoomRequest,
        @AuthenticationPrincipal user: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val dto = lectureService.createLectureRoom(user.getId(), request)
        return ResponseEntity.ok(
            mapOf(
                "message" to "강의가 성공적으로 생성되었습니다.",
                "data" to dto
            )
        )
    }

    @GetMapping("/{id}")
    fun getLectureRoomById(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val lecture = lectureService.getLectureRoomById(id)
        return ResponseEntity.ok(
            mapOf(
                "message" to "강의 조회 성공",
                "data" to lecture
            )
        )
    }


    @GetMapping("/list")
    fun getAllLectureRooms(): ResponseEntity<Map<String, Any>> {
        val lectures = lectureService.getAllLectureRooms()
        return ResponseEntity.ok(
            mapOf(
                "message" to "강의 목록 조회 성공",
                "data" to lectures
            )
        )
    }

    @GetMapping("/posts")
    fun getLectureRoomPosts(
        @RequestParam lectureId: Long,
        @RequestParam type: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val postType = LecturePostType.from(type)
        val posts = postService.getPostsByLectureAndPostType(lectureId, postType!!, userDetails.member.id)
        return ResponseEntity.ok(mapOf("message" to "조회 성공", "data" to posts))
    }

}

data class CreateLectureRoomRequest(
    val title: String,
    val grade: Int?,
    val semester: Int?,
    val intro: String?,
    val themeColor: String,
)