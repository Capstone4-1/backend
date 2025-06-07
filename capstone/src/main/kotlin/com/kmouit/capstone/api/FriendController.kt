package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.AcceptFriendRequestDto
import com.kmouit.capstone.dtos.FriendRequestDto
import com.kmouit.capstone.dtos.MemberSimpleDto
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/friend")
class FriendController(
    private val friendService: FriendService,
    private val memberRepository: MemberRepository,
) {

    /**
     * 회원 검색  friendController 로 이동예정
     */
    @GetMapping("/search")
    fun getMemberByNickname(@RequestParam nickname: String): ResponseEntity<MemberSimpleDto> {
        val member = memberRepository.findByNickname(nickname)
            ?: throw NoSuchElementException("대상 회원이 존재하지 않습니다")

        val memberSimpleDto = MemberSimpleDto(member)
        return ResponseEntity.ok().body(memberSimpleDto)
    }
    /**
     * 친구 요청
     */
    @PostMapping("/add-friend")
    fun addFriend(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody body: Map<String, Long>, // 👈 Map<String, Long>으로 변경
    ): ResponseEntity<Any> {
        val myId = userDetails.getId()
        val receiverId = body["id"] ?: return ResponseEntity.badRequest().body("id 누락됨")
        friendService.addFriend(myId, receiverId)
        return ResponseEntity.ok(mapOf("message" to "친구 요청 성공"))
    }
    /**
     * 친구 요청 거절
     */
    @PostMapping("/decline-friend")
    fun declineFriendRequestResponse(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody declineFriendRequestDto: FriendRequestDto,
    ): ResponseEntity<Map<String, String>> {
        val myId = userDetails.getId()
        val idToDecline = declineFriendRequestDto.idToDecline
        friendService.declineFriendRequest(myId, idToDecline)
        return ResponseEntity.ok(mapOf("message" to "친구 요청 거절 "))
    }


    /**
     * 친구 요청 수락
     */
    @PostMapping("/accept-friend")
    fun acceptFriendRequestResponse(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody acceptFriendRequestDto: AcceptFriendRequestDto,
    ): ResponseEntity<Map<String, String>> {
        val myId = userDetails.getId()
        val idToAccept = acceptFriendRequestDto.idToAccept
        friendService.acceptFriendRequest(myId, idToAccept)
        return ResponseEntity.ok(mapOf("message" to "친구 요청 수락 성공"))
    }


    /**
     * 받은 친구 요청 목록 조회
     */

    @GetMapping("/request-friend-list")
    fun getRequestFriendList(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val requestMemberDtoList = friendService.findRequestMembers(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "requestMemberList" to requestMemberDtoList,
                "message" to "친구 요청 목록 조회 성공"
            )
        )
    }

    /**
     * 친구 목록 조회
     */
    @GetMapping("/my-friends")
    fun getMyFriends(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val acceptMemberDtoList = friendService.findAcceptMembers(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "acceptMemberDtoList" to acceptMemberDtoList,
                "message" to "친구 목록 조회 성공"
            )
        )
    }


}