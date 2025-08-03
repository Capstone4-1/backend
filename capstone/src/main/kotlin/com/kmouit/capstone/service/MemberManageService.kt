package com.kmouit.capstone.service

import com.kmouit.capstone.DEFAULT_PROFILE_IMAGE_URL
import com.kmouit.capstone.DEFAULT_PROFILE_THUMBNAIL_URL
import com.kmouit.capstone.MAX_FILE_SIZE
import com.kmouit.capstone.Role.*
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.TodoDto
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.NoticeDto
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.exception.FileSizeLimitExceededException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.*
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile


@Service
@Transactional(readOnly = true)
class MemberManageService(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val memberRepository: MemberRepository,
    private val uploadService: S3UploadService,
    private val todoRepository: TodoRepository,
    private val lectureMarkInfoRepository: LectureMarkInfoRepository,
    private val boardMarkInfoRepository: BoardMarkInfoRepository,
    private val friendInfoRepository: FriendInfoRepository,

) {

    /**
     * 프로필 사진 설정
     */
    @Transactional
    fun setProfileImage(id: Long, file: MultipartFile): String {
        val maxSizeInBytes = MAX_FILE_SIZE

        if (file.size > maxSizeInBytes) {
            throw FileSizeLimitExceededException("파일 크기는 ${MAX_FILE_SIZE / 1024 / 1024}MB 이하여야 합니다.")
        }
        val member =
            memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원 : setProfileImage") }

        val (originalUrl, thumbnailUrl) = uploadService.uploadWithThumbnail(file)
        member.profileImageUrl = originalUrl  // 혹은 originalUrl, 원하는 쪽 선택
        member.thumbnailUrl = thumbnailUrl
        return originalUrl  // 또는 originalUrl
    }

    @Transactional
    fun deleteProfileImage(id: Long): String {
        val member =
            memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원 : deleteProfileImage") }
        member.profileImageUrl = DEFAULT_PROFILE_IMAGE_URL

        return member.profileImageUrl!!
    }

    /**
     * 자기 소개 설정
     */
    @Transactional
    fun setIntro(id: Long, intro: String) {
        val member = memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        member.intro = intro
    }


    /**
     * 사용자 알림 가져오기
     */
    fun getNotice(id: Long): List<NoticeDto> {
        val member = memberRepository.findMemberAndUnreadNoticesById(id)
        val notices = member?.notices ?: return emptyList()
        return notices.map { NoticeDto(it) }
    }


    @Transactional
    fun join(joinForm: JoinForm) {
        if (memberRepository.findByUsername(joinForm.username) != null) {
            throw DuplicateUsernameException("이미 가입된 id 입니다")
        }
        val member = Member(
            username = joinForm.username,
            password = passwordEncoder.encode(joinForm.password),
            name = joinForm.name,
            email = joinForm.email,
            nickname = joinForm.name,
            profileImageUrl = DEFAULT_PROFILE_IMAGE_URL,
            thumbnailUrl = DEFAULT_PROFILE_THUMBNAIL_URL,
            intro = "${joinForm.name}입니다. 잘 부탁드립니다."
        )
        member.roles.add(USER)
        member.roles.add(STUDENT)
        memberRepository.save(member)
    }

    /**
     * todolist 가져오기
     */
    fun getTodo(id: Long): List<TodoDto> {
        return todoRepository.findByMemberId(id)
            .orEmpty()
            .map { TodoDto(it) }
    }

    @Transactional
    fun deleteTodo(todoId: Long, memberId: Long) {
        val todo = todoRepository.findById(todoId).orElseThrow {
            IllegalArgumentException("해당 Todo가 존재하지 않습니다.")
        }

        if (todo.member?.id != memberId) {
            throw IllegalAccessException("권한이 없습니다.")
        }
        todoRepository.delete(todo)
    }


    @Transactional
    fun setNickname(memberId: Long, newNickname: String) {
        val trimmed = newNickname.trim()

        val nicknameRegex = Regex("^[a-zA-Z0-9가-힣]{1,10}$")
        if (!nicknameRegex.matches(trimmed)) {
            throw IllegalArgumentException("닉네임은 1~10자의 공백·특수문자 없는 한글, 영어, 숫자만 허용됩니다.")
        }

        if (memberRepository.existsByNickname(trimmed)) {
            throw IllegalStateException("중복된 닉네임입니다.")
        }

        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("회원 정보를 찾을 수 없습니다.") }

        member.nickname = trimmed
    }

    fun checkPassword(member: Member, rawPassword: String): Boolean {
        val trimmedRaw = rawPassword.trim()
        return passwordEncoder.matches(trimmedRaw, member.password)
    }


    @Transactional
    fun resetPassword(member: Member, newPassword: String) {
        val targetMember= memberRepository.findById(member.id!!).orElseThrow { NoSearchMemberException(HttpStatus.NOT_FOUND, "존재하지않는 회원") }
        try {
            val encodedNewPassword = passwordEncoder.encode(newPassword)
            targetMember.password = encodedNewPassword
        } catch ( e : Exception){
            throw Exception("비밀번호 재설정 오류")
        }
    }

    @Transactional
    fun resetEmail(member: Member, newEmail: String) {
        val targetMember= memberRepository.findById(member.id!!).orElseThrow { NoSearchMemberException(HttpStatus.NOT_FOUND, "존재하지않는 회원") }
        try {
            targetMember.email = newEmail
        } catch ( e : Exception){
            throw Exception("이메일 재설정 오류")
        }

    }
    @Transactional
    fun withdraw(member: Member) {
        try {
            val managedMember = memberRepository.findById(member.id!!)
                .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다.") }

            managedMember.notices.forEach { it.member = null }
            managedMember.notices.clear()
            lectureMarkInfoRepository.deleteAllByMember(managedMember)
            boardMarkInfoRepository.deleteAllByMember(managedMember)
            friendInfoRepository.deleteAllByFriendInfoIdSendMember(managedMember)
            friendInfoRepository.deleteAllByFriendInfoIdReceiveMember(managedMember)
            memberRepository.delete(managedMember)

        } catch (e: Exception) {
            throw Exception("탈퇴 오류: ${e.message}")
        }
    }
}