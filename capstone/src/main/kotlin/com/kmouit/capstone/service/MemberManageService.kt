package com.kmouit.capstone.service

import com.kmouit.capstone.DEFAULT_PROFILE_IMAGE_URL
import com.kmouit.capstone.DEFAULT_PROFILE_THUMBNAIL_URL
import com.kmouit.capstone.MAX_FILE_SIZE
import com.kmouit.capstone.Role.*
import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.domain.jpa.TodoDto
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.NoticeDto
import com.kmouit.capstone.exception.FileSizeLimitExceededException
import com.kmouit.capstone.exception.NoSearchMemberException
import com.kmouit.capstone.repository.jpa.*
import jakarta.persistence.EntityManager
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime


@Service
@Transactional(readOnly = true)
class MemberManageService(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val memberRepository: MemberRepository,
    private val uploadService: S3UploadService,
    private val todoRepository: TodoRepository,
    private val friendInfoRepository: FriendInfoRepository,
    private val postScrapInfoRepository: PostScrapInfoRepository,
    private val postLikeInfoRepository: PostLikeInfoRepository,
    private val s3UploadService: S3UploadService,
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val em : EntityManager
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

        val member = memberRepository.findById(id)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원 : setProfileImage") }

        // 기존 이미지가 기본 이미지가 아니면 S3에서 삭제
        if (!member.profileImageUrl.isNullOrBlank() && member.profileImageUrl != DEFAULT_PROFILE_IMAGE_URL) {
            uploadService.deleteS3Object(uploadService.extractS3KeyFromUrl(member.profileImageUrl!!))
            uploadService.deleteS3Object(uploadService.extractS3KeyFromUrl(member.thumbnailUrl!!))
        }


        val (originalUrl, thumbnailUrl) = uploadService.uploadProfileImageWithThumbnail(file)
        member.profileImageUrl = originalUrl
        member.thumbnailUrl = thumbnailUrl

        return originalUrl
    }

    @Transactional
    fun deleteProfileImage(id: Long): String {
        val member = memberRepository.findById(id)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원 : deleteProfileImage") }

        //  기존 이미지 삭제 (기본 이미지가 아닐 때만)
        if (!member.profileImageUrl.isNullOrBlank() && member.profileImageUrl != DEFAULT_PROFILE_IMAGE_URL) {
            uploadService.deleteS3Object(uploadService.extractS3KeyFromUrl(member.profileImageUrl!!))
            uploadService.deleteS3Object(uploadService.extractS3KeyFromUrl(member.thumbnailUrl!!))
        }


        member.profileImageUrl = DEFAULT_PROFILE_IMAGE_URL
        member.thumbnailUrl = DEFAULT_PROFILE_THUMBNAIL_URL

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
        val member = Member(
            username = joinForm.username,
            password = passwordEncoder.encode(joinForm.password),
            name = joinForm.name,
            email = joinForm.email,
            nickname = joinForm.nickname,
            joinDate = LocalDateTime.now(),
            profileImageUrl = DEFAULT_PROFILE_IMAGE_URL,
            thumbnailUrl = DEFAULT_PROFILE_THUMBNAIL_URL,
            intro = "${joinForm.nickname}입니다. 잘 부탁드립니다."
        )
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
        val managedMember = memberRepository.findById(member.id!!)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다.") }

        // 1️⃣ 회원 게시글 삭제
        val userPosts = postRepository.findAllByMember(managedMember)
        userPosts.forEach { post ->
            // S3 이미지 삭제
            s3UploadService.deleteAllImages(post.imageUrls, post.thumbnailUrl)

            // 좋아요 / 스크랩 삭제
            postLikeInfoRepository.deleteByPostId(post.id!!)
            postScrapInfoRepository.deleteByPostId(post.id!!)

            // 게시글 댓글 삭제
        }

        // DB에서 게시글 삭제 (한 번에 flush)
        postRepository.deleteAll(userPosts)
        postRepository.flush()

        // 2️⃣ 회원이 쓴 댓글 삭제 (남은 것)
        commentRepository.deleteAllByMember(managedMember)
        commentRepository.flush()

        // 3️⃣ 친구 정보 삭제
        friendInfoRepository.deleteAllByFriendInfoIdSendMember(managedMember)
        friendInfoRepository.deleteAllByFriendInfoIdReceiveMember(managedMember)
        friendInfoRepository.flush()

        // 4️⃣ 회원 삭제
        memberRepository.delete(managedMember)
        memberRepository.flush()
    }



    @Transactional
    fun refreshRecentLoginTime(id: Long) {
        val member = memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        member.lastLoginAt = LocalDateTime.now()
    }

    fun isEmailRegistered(email: String): Boolean {
        return memberRepository.existsByEmail(email)
    }

    fun isUsernameRegistered(username: String): Boolean {
        return memberRepository.existsByUsername(username)
    }
    fun isNicknameRegistered(nickname: String): Boolean {
        return memberRepository.existsByNickname(nickname)
    }
}