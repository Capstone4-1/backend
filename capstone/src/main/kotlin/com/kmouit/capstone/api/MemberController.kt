package com.kmouit.capstone.api

import com.kmouit.capstone.TodoItemStatus
import com.kmouit.capstone.domain.Todo
import com.kmouit.capstone.domain.TodoDto
import com.kmouit.capstone.dtos.*
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.TodoRepository
import com.kmouit.capstone.service.MemberManageService
import com.kmouit.capstone.service.S3UploadService
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
) {

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
        val newNickname = payload["nickname"]
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "nickname 누락"))
        if (memberRepository.existsByNickname(newNickname)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "중복된 닉네임 입니다."))
        }
        val member = memberRepository.findById(userDetails.getId())
            .orElseThrow { NoSuchElementException("회원 정보를 찾을 수 없습니다.") }

        member.nickname = newNickname
        memberRepository.save(member)
        return ResponseEntity.ok(mapOf("message" to "별명 수정 성공"))
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