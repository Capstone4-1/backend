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
    val mailService: MailService,
) {

    @PostMapping("/new")
    fun responseNewRoom(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam username: String,
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
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        mailService.exitMailRoom(id)
        println("삭제 요청: roomId=$id, 요청자=${userDetails.getName()}")
        return ResponseEntity.ok(
            mapOf(
                "message" to "채팅방 삭제 성공",
            )
        )
    }

    /**
     * 채팅방 메시지들 조회
     */
    @GetMapping("/messages/{roomId}")
    fun responseGetMessages(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val messages = mailService.searchMessages(roomId, userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "messages" to messages,
                "message" to "채팅방 목록 조회 성공"
            )
        )
    }

    /**
     * 메일보내기 방 id, 상대방 id
     */
    @PostMapping("send-mail/{roomId}")
    fun sendMail(
        @PathVariable roomId: Long,
        @RequestBody request: SendMailRequest,
        @AuthenticationPrincipal user: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        println("홏풀------------------------------------------------")
        mailService.sendMessage(roomId, user.member.id!!, request.partnerId, request.content)
        return ResponseEntity.ok(
            mapOf(
                "message" to "메일 보내기 성공 : ${request.content}"
            )
        )
    }

    data class SendMailRequest(val content: String, val partnerId: Long)


    /**
     * 메일 개수 체크
     */
    @GetMapping("/check-new")
    fun checkMail(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Map<String, Int>> {
        val newMailCount = mailService.countNewMail(userDetails.member.id!!)
        return ResponseEntity.ok(mapOf("newMailCount" to newMailCount))
    }

    @PutMapping("/read-room/{roomId}")
    fun readAllMailsInRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<String> {
        mailService.markAllAsRead(roomId, userDetails.member.id!!)
        return ResponseEntity.ok("읽음 처리 완료")
    }
}