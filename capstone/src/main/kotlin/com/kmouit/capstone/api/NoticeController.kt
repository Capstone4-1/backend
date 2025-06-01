package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.ReadAllNoticeRequest
import com.kmouit.capstone.service.NoticeService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api/notice")
class NoticeController(
    private val noticeService: NoticeService

) {

    /**
     * 개별 알림 읽음 처리
     */
    @PostMapping("read/{id}")
    fun responseReadNotices(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, String>> {
        noticeService.readNotice(id)
        return ResponseEntity.ok().body(
            mapOf("message" to "알림 읽음 성공")
        )
    }


    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("read-all")
    fun responseReadAllNotices(
        @RequestBody request : ReadAllNoticeRequest
    ): ResponseEntity<Map<String, String>> {
        noticeService.readAllNotice(request.noticeIds)
        return ResponseEntity.ok().body(
            mapOf("message" to "알림 모두 읽음 성공")
        )
    }

}