package com.kmouit.capstone.api

import com.kmouit.capstone.LecturePostType
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.LectureService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/lecture-room")
class LectureRoomController(
    private val lectureService: LectureService,
    private val postService: LectureService,
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
        val lectureWithMark = lectureService.getLectureRoomById(id, user.member.id!!)

        return ResponseEntity.ok(
            mapOf(
                "message" to "강의 조회 성공",
                "data" to lectureWithMark,
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


    //강의실 즐겨찾기 조회
    @GetMapping("/mark")
    fun getMyLectureFavorites(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val markedLecture = lectureService.findMyLectureFavorites(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "강의실 즐겨찾기 조회 성공",
                "markedLecture" to markedLecture
            )
        )
    }

    // 강의실 즐겨찾기 추가
    @PostMapping("/mark")
    fun addLectureFavorite(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam lectureRoomId: Long
    ): ResponseEntity<Map<String, String>> {
        lectureService.saveLectureMark(userDetails.getId(), lectureRoomId)
        return ResponseEntity.ok(
            mapOf("message" to "강의실 즐겨찾기 추가 성공")
        )
    }

    // 강의실 즐겨찾기 삭제
    @DeleteMapping("/mark")
    fun deleteLectureFavorite(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam lectureRoomId: Long
    ): ResponseEntity<Map<String, String>> {
        lectureService.deleteLectureMark(userDetails.getId(), lectureRoomId)
        return ResponseEntity.ok(
            mapOf("message" to "강의실 즐겨찾기 삭제 성공")
        )
    }

}

data class CreateLectureRoomRequest(
    val title: String,
    val grade: Int?,
    val semester: Int?,
    val intro: String?,
    val themeColor: String,
)