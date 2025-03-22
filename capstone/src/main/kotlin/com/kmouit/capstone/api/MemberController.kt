package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.LoginForm
import com.kmouit.capstone.dtos.MemberDto
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.MemberManageService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberRepository: MemberRepository,
    private val memberManageService: MemberManageService
) {
    /**
     * 회원 검색
     */
    @GetMapping("/search")
    fun getMemberByStudentId(@RequestParam studentId: String): ResponseEntity<MemberDto> {
        val member = memberRepository.findByUsername(studentId)
            ?: throw NoSuchElementException("대상 회원이 존재하지 않습니다")
        return ResponseEntity.ok().body(MemberDto(member.id!!, member.name!!, member.username!!))
    }

    /**
     * 회원가입
     */
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