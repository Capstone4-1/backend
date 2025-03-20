package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.LoginForm
import com.kmouit.capstone.dtos.MemberDto
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.RefreshTokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberRepository: MemberRepository,
    private val memberManageService: MemberManageService
){

    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    fun getMemberByStudentId(@RequestParam studentId: String): ResponseEntity<MemberDto> {

        println("member 조회 호출")
        val member = memberRepository.findByUsername(studentId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(MemberDto(member.name!!, member.username!!))
    }



    @PostMapping("/join")
    fun join(@RequestBody joinForm: JoinForm): ResponseEntity<String> {
        println("회원가입 호출")
        try {
            memberManageService.join(joinForm)
        } catch (e: DuplicateUsernameException) {

        }
        return ResponseEntity.ok("회원 가입 성공")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginForm: LoginForm): ResponseEntity<Any> {
        println("===로그인 호출===")
        println(loginForm)
        return ResponseEntity.ok("로그인 요청 받음")
    }


    @PostMapping("/logout")
    fun logout(@RequestBody logoutRequest: Map<String, String>) {

    }
}