package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.AcceptFriendRequestDto
import com.kmouit.capstone.dtos.FriendRequestDto
import com.kmouit.capstone.dtos.MemberDto
import com.kmouit.capstone.repository.FriendInfoRepository
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.FriendService
import org.springframework.http.HttpStatus
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
    @PostMapping("/{id}/decline-friend")
    fun declineFriendRequestResponse(
        @PathVariable id: Long,
        @RequestBody declineFriendRequestDto: FriendRequestDto
    ): ResponseEntity<Map<String, Unit>> {
        println("친구 요청 거절 호출")
        val idToDecline = declineFriendRequestDto.idToDecline
        return try {
            val requestMemberDtoList = friendService.declineFriendRequest(id, idToDecline)
            ResponseEntity.status(HttpStatus.OK).body(mapOf("requestMemberList" to requestMemberDtoList))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyMap())
        }
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/{id}/accept-friend")
    fun acceptFriendRequestResponse(
        @PathVariable id: Long,
        @RequestBody acceptFriendRequestDto: AcceptFriendRequestDto
    ) {
        println("친구 요청 수락 호출")
        val idToAccept = acceptFriendRequestDto.idToAccept
        println(idToAccept)
        try {
            friendService.acceptFriendRequest(id, idToAccept)
            ResponseEntity.ok()
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
        }
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/request-friend-list")
    fun getRequestFriendList(@PathVariable id: Long): ResponseEntity<Map<String,Any>> {
        println("친구요청 목록 조회 호출")
        try{
            val requestMemberDtoList = friendService.findRequestMembers(id)
             return ResponseEntity.status(HttpStatus.OK).body(
                 mapOf(
                     "requestMemberList" to requestMemberDtoList,
                     "message" to "친구 요청 목록 조회 호출 성공"))
        } catch (e: Exception) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyMap())
        }
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/accept-friend-list")
    fun getAcceptFriendList(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        println("친구 목록 조회 호출")
        try {
            val acceptMemberDtoList = friendService.findAcceptMembers(id)
            return ResponseEntity.status(HttpStatus.OK)
                .body(
                    mapOf(
                        "acceptMemberDtoList" to acceptMemberDtoList,
                        "message" to "친구 목록 조회 호출 성공"
                    ))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    mapOf(
                        "error" to "친구 목록 조회 실패",
                        "message" to (e.message ?: "알 수 없는 오류"),
                    )
                )
        }
    }


    @PreAuthorize("permitAll()")
    @PostMapping("/{id}/add-friend")
    fun addFriend(
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Map<String, Any>> {
        try {
            val studentId = body["studentId"]!!
            friendService.addFriend(id, studentId)
            return ResponseEntity.ok(mapOf("message" to "요청 완료"))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    mapOf(
                        "error" to "친구 추가 실패",
                        "message" to (e.message ?: "알수없는 오류"),
                    )
                )
        }
    }


}