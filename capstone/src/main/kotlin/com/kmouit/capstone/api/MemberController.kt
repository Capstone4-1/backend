package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.*
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.S3UploadService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberRepository: MemberRepository,
    private val memberManageService: MemberManageService,
    private val s3Service: S3UploadService,
) {

    /**
     *  새로고침시 정보 불러오기
     */
    @GetMapping("/me")
    fun responseMe(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<MeDto> {
        val member = userDetails.member
        return ResponseEntity.ok().body(MeDto(member))
    }


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
    @GetMapping("/my-notices")
    fun responseGetMyNotices(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, List<NoticeDto>>> {
        val dtoList = memberManageService.getNotice(userDetails.getId())
        return ResponseEntity.ok(mapOf("notices" to dtoList))
    }


    @PostMapping("/set-intro")
    fun responseSetIntro(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: IntroRequest,

    ): ResponseEntity<Map<String, String>> {
        memberManageService.setIntro(userDetails.getId(), request.intro)
        return ResponseEntity.ok(
            mapOf("message" to "intro 수정 success")
        )
    }

    @PostMapping("/set-profile-image")
    fun responseSetProfileImage(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestPart("profileImage") profileImage: MultipartFile,
    ): ResponseEntity<Map<String, String>> {
        val profileImageUrl = memberManageService.setProfileImage(userDetails.getId(), profileImage)
        return ResponseEntity.ok(
            mapOf(
                "message" to "프로필 이미지가 성공적으로 수정되었습니다.",
                "imageUrl" to profileImageUrl
            )
        )
    }

    @DeleteMapping("/delete-profile-image")
    fun deleteProfileImage(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {
        val defaultImageUrl = memberManageService.deleteProfileImage(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "프로필 이미지가 삭제되었습니다.",
                "imageUrl" to defaultImageUrl  // ✅ 기본 이미지 URL을 응답에 포함
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