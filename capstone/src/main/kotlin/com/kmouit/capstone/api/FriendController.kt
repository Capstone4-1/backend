package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.RequestMemberDto
import com.kmouit.capstone.repository.FriendInfoRepository
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.FriendService
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/friend")
class FriendController(
    private val memberRepository: MemberRepository,
    private val friendService: FriendService,
    private val friendInfoRepository: FriendInfoRepository
) {

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/request-friend-list")
    fun getFriendRequestList(@PathVariable id: Long): ResponseEntity<Map<String, List<RequestMemberDto>>> {
        println("친구요청 목록 조회 호출")
        return try {
            val requestMemberDtoList = friendService.findRequestMembers(id)
            ResponseEntity.status(HttpStatus.OK).body(mapOf("requestMemberList" to requestMemberDtoList))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyMap())
        }
    }


    @PreAuthorize("permitAll()")
    @PostMapping("/{id}/add-friend")
    fun addFriend(
        @PathVariable id :Long,
        @RequestBody body: Map<String, String>
    ): Any {
        try {
            val studentId = body["studentId"]!!
            friendService.addFriend(id, studentId)
        } catch (e: Exception) {
            println(e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생")
        }
        return ResponseEntity.ok("요청이 완료되었습니다")

    }




}