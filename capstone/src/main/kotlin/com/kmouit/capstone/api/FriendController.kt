package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.AcceptFriendRequestDto
import com.kmouit.capstone.dtos.FriendRequestDto
import com.kmouit.capstone.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/friend")
class FriendController(
    private val friendService: FriendService,
) {

    /**
     * 친구 요청
     */
    @PostMapping("/{id}/add-friend")
    fun addFriend(
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<String> {
        val studentId = body["studentId"]!!
        friendService.addFriend(id, studentId)
        return ResponseEntity.ok().body("친구 요청 success")
    }


    /**
     * 친구 요청 거절
     */
    @PostMapping("/{id}/decline-friend")
    fun declineFriendRequestResponse(
        @PathVariable id: Long,
        @RequestBody declineFriendRequestDto: FriendRequestDto
    ): ResponseEntity<Map<String, String>> {
        val idToDecline = declineFriendRequestDto.idToDecline
        friendService.declineFriendRequest(id, idToDecline)
        return ResponseEntity.ok().body(mapOf("message" to "친구 요청 거절 success"))
    }


    /**
     * 친구 요청 수락
     */
    @PostMapping("/{id}/accept-friend")
    fun acceptFriendRequestResponse(
        @PathVariable id: Long,
        @RequestBody acceptFriendRequestDto: AcceptFriendRequestDto
    ): ResponseEntity<Map<String, String>> {
        val idToAccept = acceptFriendRequestDto.idToAccept
        println(idToAccept)
        friendService.acceptFriendRequest(id, idToAccept)
        return ResponseEntity.ok().body(mapOf("message" to "친구 요청 수락 success"))
    }


    @GetMapping("/{id}/request-friend-list")
    fun getRequestFriendList(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val requestMemberDtoList = friendService.findRequestMembers(id)
        return ResponseEntity.ok().body(
            mapOf(
                "requestMemberList" to requestMemberDtoList,
                "message" to "친구 요청 목록 조회 success"
            )
        )
    }


    /**
     * 친구 목록 조회
     */
    @GetMapping("/{id}/accept-friend-list")
    fun getAcceptFriendList(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        println("친구 목록 조회 호출")
        val acceptMemberDtoList = friendService.findAcceptMembers(id)
        return ResponseEntity.ok().body(
            mapOf(
                "acceptMemberDtoList" to acceptMemberDtoList,
                "message" to "친구 목록 조회 호출 success"
            )
        )
    }


}