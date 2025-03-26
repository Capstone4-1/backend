package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.AcceptFriendRequestDto
import com.kmouit.capstone.dtos.FriendRequestDto
import com.kmouit.capstone.dtos.MemberSimpleDto
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/friend")
class FriendController(
    private val friendService: FriendService,
    private val memberRepository: MemberRepository
) {

    /**
     * 회원 검색  friendController 로 이동예정
     */
    @GetMapping("/search")
    fun getMemberByStudentId(@RequestParam studentId: String): ResponseEntity<MemberSimpleDto> {
        val member = memberRepository.findByUsername(studentId)
            ?: throw NoSuchElementException("대상 회원이 존재하지 않습니다")
        return ResponseEntity.ok().body(MemberSimpleDto(member.id!!, member.name!!, member.username!!))
    }

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
        friendService.acceptFriendRequest(id, idToAccept)
        return ResponseEntity.ok().body(mapOf("message" to "친구 요청 수락 success"))
    }

    /**
     * 받은 친구 요청 목록 조회
     */
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
        val acceptMemberDtoList = friendService.findAcceptMembers(id)
        return ResponseEntity.ok().body(
            mapOf(
                "acceptMemberDtoList" to acceptMemberDtoList,
                "message" to "친구 목록 조회 호출 success"
            )
        )
    }


}