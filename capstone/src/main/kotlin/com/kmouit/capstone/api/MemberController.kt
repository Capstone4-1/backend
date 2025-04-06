package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.*
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.UploadService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberRepository: MemberRepository,
    private val memberManageService: MemberManageService,
    private val s3Service: UploadService,
) {

    /**
     *  유저 정보 조회
     */
    @GetMapping("/search")
    fun searchMemberInfo(@RequestParam studentId: String): ResponseEntity<MemberDto> {
        val member = memberRepository.findByUsername(studentId)
            ?: throw NoSuchElementException("대상 회원이 존재하지 않습니다")
        return ResponseEntity.ok().body(MemberDto(member))
    }

    /**
     *  온 알림 조회
     */
    @GetMapping("/{id}/notice")
    fun responseGetNotices(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, List<NoticeDto>>> {
        val dtoList = memberManageService.getNotice(id)
        return ResponseEntity.ok().body(
            mapOf("notices" to dtoList)
        )
    }

    @PostMapping("/{id}/set-intro")
    fun responseSetIntro(
        @PathVariable id: Long,
        @RequestBody request: IntroRequest,
    ): ResponseEntity<Map<String, String>> {
        memberManageService.setIntro(id, request.intro)
        return ResponseEntity.ok().body(
            mapOf(
                "message" to "intro 수정 success")
        )
    }

    @PostMapping("/{id}/set-profile-image")
    fun responseSetProfileImage(
        @PathVariable id: Long,
        @RequestPart("profileImage") profileImage: MultipartFile,
    ): ResponseEntity<Map<String, String>> {


        val profileImageUrl = memberManageService.setProfileImage(id, profileImage)
        return ResponseEntity.ok().body(
            mapOf(
                "message" to "프로필 이미지가 성공적으로 수정되었습니다.",
                "imageUrl" to profileImageUrl // 원한다면 여기 포함
            )
        )
    }

    @PostMapping("/join")
    fun join(@RequestBody joinForm: JoinForm): ResponseEntity<Map<String, String>> {
        memberManageService.join(joinForm)
        return ResponseEntity.ok().body(
            mapOf("message" to "회원 가입 success")
        )
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(@RequestBody logoutRequest: Map<String, String>) {
        //Todo
    }


    /**
     * 로그인
     * 스프링 시큐리티로 대체
     */
//    @PostMapping("/login")
//    fun login(@RequestBody loginForm: LoginForm): ResponseEntity<Any> {
//        return ResponseEntity.ok("로그인 요청 받음")
//    }
}