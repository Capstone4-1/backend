package com.kmouit.capstone.api

import com.kmouit.capstone.Role
import com.kmouit.capstone.TodoItemStatus
import com.kmouit.capstone.config.AuthMailService
import com.kmouit.capstone.domain.jpa.FriendSummaryDto
import com.kmouit.capstone.domain.jpa.Todo
import com.kmouit.capstone.domain.jpa.TodoDto
import com.kmouit.capstone.dtos.*
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.jpa.FriendInfoRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
import com.kmouit.capstone.repository.jpa.TodoRepository
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.RefreshTokenService
import org.springframework.http.HttpStatus
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
    private val todoRepository: TodoRepository,
    private val friendInfoRepository: FriendInfoRepository,
    private val refreshTokenService: RefreshTokenService,
    private val authMailService: AuthMailService,

    ) {

    @GetMapping("/check-role")
    fun checkRole(
        @AuthenticationPrincipal userDetails: CustomUserDetails?,
        @RequestParam role: String,
    ): ResponseEntity<out Map<String, Any>> {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(mapOf("hasRole" to false))
        }

        val member = userDetails.member
        val requiredRole = Role.from(role)
        val hasRole = requiredRole?.let { r -> member.roles.any { it.name == r.name } } ?: false

        return ResponseEntity.ok(
            mapOf(
                "hasRole" to hasRole,
                "message" to "권한 인증 성공",
            )
        )
    }


    @GetMapping("/my-roles")
    fun getMyRoles(@AuthenticationPrincipal userDetails: CustomUserDetails?): ResponseEntity<Any> {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(mapOf("roles" to emptyList<String>()))
        }
        val roles = userDetails.member.roles.map { it.name }
        return ResponseEntity.ok(mapOf("roles" to roles))
    }

    /**
     * 비밀번호찾기 아이디 검증
     * 아이디 검증 후 그 아이디의 이메일로 코드 발송
     */
    @PostMapping("/verify-id")
    fun verifyIdAndSendCode(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val username = request["username"]
        if (username.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "아이디가 입력되지 않았습니다."))
        }

        val member = memberRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "일치하는 회원정보가 없습니다. 다시 입력해주세요."))

        val email = member.email
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "이메일이 등록되어 있지 않습니다."))

        authMailService.sendSimpleMessage(email)

        // 이메일도 반환
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "인증코드를 이메일로 발송했습니다.",
                "email" to email
            )
        )
    }

    @PostMapping("/verify-password")
    fun verifyPassword(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: Map<String, String>,
    ): ResponseEntity<Map<String, Boolean>> {
        val rawPassword = request["password"]
            ?: return ResponseEntity.badRequest().body(mapOf("message" to false))

        val member = userDetails.member
        return if (memberManageService.checkPassword(member, rawPassword)) {
            ResponseEntity.ok(mapOf("message" to true))
        } else {
            println("실패=========")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to false))
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        val rawNewPassword = request["newPassword"]
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "passWord 재설정 실패"))
        memberManageService.resetPassword(userDetails.member, rawNewPassword)
        return ResponseEntity.ok(mapOf("message" to "passWord 재설정 완료"))
    }

    @PostMapping("/reset-password/no-login")
    fun resetPasswordWithoutLogin(
        @RequestBody request: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        val username = request["username"] ?: return ResponseEntity.badRequest().body(mapOf("message" to "username 누락"))
        val newPassword =
            request["newPassword"] ?: return ResponseEntity.badRequest().body(mapOf("message" to "새 비밀번호 누락"))

        val member = memberRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "가입된 회원이 없습니다."))

        memberManageService.resetPassword(member, newPassword)
        return ResponseEntity.ok(mapOf("message" to "비밀번호 재설정 완료"))
    }


    @PostMapping("/reset-email")
    fun resetEmail(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        val newEmail = request["newEmail"]
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "Email 재설정 실패"))
        memberManageService.resetEmail(userDetails.member, newEmail)
        return ResponseEntity.ok(mapOf("message" to "Email 재설정 완료"))
    }


    /**
     *  새로고침시 정보 불러오기
     */
    @GetMapping("/me")
    fun responseMe(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Map<String, Any>> {
        val member = userDetails.member
        val meDto = MeDto(member)
        return ResponseEntity.ok(
            mapOf(
                "meDto" to meDto,
                "message" to "me 조회 성공"
            )
        )
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
     *  모달용 유저 정보 조회
     */
    @GetMapping("/summary/{userId}")
    fun getMemberInfo(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<FriendSummaryDto> {
        val member = memberRepository.findById(userId)
            .orElseThrow { NoSuchElementException("대상 회원이 존재하지 않습니다") }

        val currentUserId = userDetails.getId()
        val isFriend = friendInfoRepository.areFriends(currentUserId, userId)
        return ResponseEntity.ok(FriendSummaryDto(member, isFriend))
    }


    /**
     *  온 알림 조회
     */
    @GetMapping("/my-notices")
    fun responseGetMyNotices(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val dtoList = memberManageService.getNotice(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "notices" to dtoList,
                "message" to "알림 조회 성공"
            )
        )
    }

    /**
     * todolist 조회
     */
    @GetMapping("/todo/my")
    fun responseGetTodo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val dtoList = memberManageService.getTodo(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "todos" to dtoList,
                "message" to "Todos 조회 성공"
            )
        )
    }

    /**
     * TodoItem 추가 하기
     */
    @PostMapping("/todo/add")
    fun addTodo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody dto: TodoDto,
    ): ResponseEntity<Map<String, Any>> {
        val member = userDetails.member
        val todo = Todo(
            content = dto.content,
            member = member,
            status = dto.status,
            dueDate = dto.dueDate
        )
        val saved = todoRepository.save(todo)

        return ResponseEntity.ok(
            mapOf(
                "message" to "Todo 추가 성공",
                "savedTodo" to saved
            )
        )

    }

    /**
     * todolist 체크
     */
    @PostMapping("/todo/{id}/status")
    fun updateStatus(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        val todo = todoRepository.findById(id).orElseThrow {
            IllegalArgumentException("해당 ID의 투두 항목이 존재하지 않습니다.")
        }
        if (todo.member?.id != userDetails.member.id) {
            return ResponseEntity.status(403).body(mapOf("message" to "권한이 없습니다."))
        }

        val newStatus = try {
            TodoItemStatus.valueOf(body["status"] ?: "")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(mapOf("message" to "유효하지 않은 상태입니다."))
        }

        todo.status = newStatus
        todoRepository.save(todo)

        return ResponseEntity.ok(mapOf("message" to "Todo 상태 변경 성공"))
    }


    @DeleteMapping("/todo/{id}")
    fun deleteTodo(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, String>> {
        memberManageService.deleteTodo(id, userDetails.getId())
        return ResponseEntity.ok(mapOf("message" to "Todo 삭제 성공"))

    }


    @PostMapping("/set-nickname")
    fun responseSetNickname(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody payload: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        val newNickname = payload["nickname"]?.trim()
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "nickname 누락"))

        return try {
            memberManageService.setNickname(userDetails.getId(), newNickname)
            ResponseEntity.ok(mapOf("message" to "별명 수정 성공"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message.toString()))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to e.message.toString()))
        }
    }


    @PostMapping("/set-intro")
    fun responseSetIntro(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: IntroRequest,

        ): ResponseEntity<Map<String, String>> {
        memberManageService.setIntro(userDetails.getId(), request.intro)
        return ResponseEntity.ok(
            mapOf("message" to "Intro 수정 성공")
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
                "message" to "Profile img 수정 성공",
                "imageUrl" to profileImageUrl
            )
        )
    }

    @DeleteMapping("/delete-profile-image")
    fun deleteProfileImage(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val defaultImageUrl = memberManageService.deleteProfileImage(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "Profile img 삭제 성공",
                "imageUrl" to defaultImageUrl  // ✅ 기본 이미지 URL을 응답에 포함
            )
        )
    }

    @PostMapping("/join")
    fun join(@RequestBody joinForm: JoinForm): ResponseEntity<Map<String, String>> {
        memberManageService.join(joinForm)
        return ResponseEntity.ok().body(
            mapOf("message" to "회원 가입 성공")
        )
    }

    @DeleteMapping
    fun withdraw(@AuthenticationPrincipal userDetails: CustomUserDetails) {
        memberManageService.withdraw(userDetails.member)
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<String> {
        val username = userDetails.username
        // 1. 메모리/레디스에 저장된 리프레시 토큰 제거
        refreshTokenService.deleteRefreshToken(username)
        memberManageService.refreshRecentLoginTime(userDetails.getId())

        return ResponseEntity.ok("Logout successful")
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
