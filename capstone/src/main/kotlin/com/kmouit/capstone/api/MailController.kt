package com.kmouit.capstone.api

import com.kmouit.capstone.domain.Mail
import com.kmouit.capstone.domain.MailDto
import com.kmouit.capstone.domain.toDto
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
        @RequestParam id: Long,
    ): ResponseEntity<Map<String, Any>> {
        mailService.createMailRoom(userDetails.getId(), id)
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
     * 채팅방 메시지들 조회 (페이징
     */
    @GetMapping("/messages/{roomId}")
    fun responseGetMessages(
        @PathVariable roomId: Long,
        @RequestParam(required = false) beforeId: Long?,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val messages = mailService.searchMessagesBeforeId(roomId, userDetails.getId(), beforeId, size)

        return ResponseEntity.ok(
            mapOf(
                "message" to "메일 페이지 가져오기 성공",
                "messages" to messages,
                "last" to (messages.size < size)
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
    ): ResponseEntity<Map<String, Any>> {
        val sent = mailService.sendMessage(roomId, user.member.id!!, request.partnerId, request.content)

        val dto = sent.toDto()
        return ResponseEntity.ok(
            mapOf(
                "message" to "메일 보내기 성공",
                "sentMessage" to dto
            )
        )
    }
    data class SendMailRequest(val content: String, val partnerId: Long)


    /**
     * 메일 개수 체크
     */
    @GetMapping("/check-new")
    fun checkMail(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Map<String, Any>> {
        val newMailCount = mailService.countNewMail(userDetails.member.id!!)
        return ResponseEntity.ok(
            mapOf(
                "newMailCount" to newMailCount,
                "message" to "메일 보내기 성공"
            )
        )
    }

    @PutMapping("/read-room/{roomId}")
    fun readAllMailsInRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        mailService.markAllAsRead(roomId, userDetails.member.id!!)
        return ResponseEntity.ok(
            mapOf(
                "message" to "읽음 처리 성공"
            )
        )
    }
}