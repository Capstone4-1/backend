package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.jwt.JWTUtil
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.LoginForm
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.service.MemberManageService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberManageService: MemberManageService,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager,
) {


    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admin-test")
    fun test(request: HttpServletRequest): ResponseEntity<String> { //테스트 api
        val authorizationHeader  = request.getHeader("Authorization")
        if (authorizationHeader != null){
            println("======${authorizationHeader}")
        }else{
            println("버그")
        }
        return ResponseEntity.ok("관리자 하이")
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
    fun login(@RequestBody loginform: LoginForm): ResponseEntity<Any> {
        println("===로그인 호출===")
        return try {
            // 입력받은 자격증명을 이용해 인증 객체 생성
            val authToken = UsernamePasswordAuthenticationToken(loginform.username, loginform.password)
            // 인증 매니저를 통해 인증 진행
            val authentication = authenticationManager.authenticate(authToken)
            // SecurityContext에 인증 객체 저장
            SecurityContextHolder.getContext().authentication = authentication

            // 인증 성공 시, 사용자 정보를 가져와 JWT 생성 (여기선 첫 번째 권한을 role로 사용)
            val userDetails = authentication.principal as CustomUserDetails
            // 예시로 만료시간 1시간(3600000ms)로 설정
            val jwt = jwtUtil.createJwt(userDetails.username, userDetails.authorities, 20 * 60 * 1000)
            ResponseEntity.ok(mapOf("accessToken" to jwt))
        } catch (e: Exception) {
            // 인증 실패 시 401 상태 반환
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password")
        }
    }
}