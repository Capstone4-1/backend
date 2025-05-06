package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.MailService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/mail")
class MailController(
    val mailService: MailService
) {

    @PostMapping("/new")
    fun responseNewRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam username: String
    ): ResponseEntity<Map<String, Any>> {
        mailService.createMailRoom(userDetails.username, username)
        return ResponseEntity.ok(
            mapOf(
                "message" to "채팅방 생성 완료"
            )
        )
    }


    @GetMapping("/my-room")
    fun responseMyRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val roomDtos = mailService.getMyRooms(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "roomDtos" to roomDtos,
                "message" to "채팅방 목록 조회 성공"
            )
        )
    }

    @DeleteMapping("/exit-room/{id}")
    fun responseExitRoom(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        mailService.exitMailRoom(id)
        println("삭제 요청: roomId=$id, 요청자=${userDetails.getName()}")
        return ResponseEntity.ok(
            mapOf(
                "message" to "채팅방 삭제 성공",
            )
        )
    }



}