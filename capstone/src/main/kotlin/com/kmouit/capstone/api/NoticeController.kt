package com.kmouit.capstone.api

import com.kmouit.capstone.repository.NoticeRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api/notice")
class NoticeController(
    private val noticeRepository: NoticeRepository
) {


}