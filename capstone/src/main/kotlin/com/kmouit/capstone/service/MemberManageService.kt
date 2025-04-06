package com.kmouit.capstone.service

import com.kmouit.capstone.MAX_FILE_SIZE
import com.kmouit.capstone.Role.*
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.dtos.JoinForm
import com.kmouit.capstone.dtos.NoticeDto
import com.kmouit.capstone.exception.DuplicateUsernameException
import com.kmouit.capstone.repository.MemberRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile


@Service
@Transactional
class MemberManageService(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val memberRepository: MemberRepository,
    private val uploadService: UploadService,
) {


    /**
     * 프로필 사진 설정
     */
    @Transactional
    fun setProfileImage(id: Long, file: MultipartFile): String {
        val maxSizeInBytes = MAX_FILE_SIZE // 1MB
        if (file.size > maxSizeInBytes) {
            throw IllegalArgumentException("파일 크기는 ${MAX_FILE_SIZE / 1024 / 1024}MB 이하여야 합니다.")
        }
        val member =
            memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원 : setProfileImage") }
        val imageUrl = uploadService.uploadProfileImage(file)
        member.profileImageUrl = imageUrl
        return imageUrl
    }

    /**
     * 자기 소개 설정
     */
    fun setIntro(id: Long, intro: String) {
        val member = memberRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 회원") }
        member.intro = intro
    }


    /**
     * 사용자 알림 가져오기
     */
    fun getNotice(id: Long): List<NoticeDto> {
        try {
            val member = memberRepository.findMemberAndUnreadNoticesById(id)
            val notices = member!!.notices
            return notices.map { NoticeDto(it) }
        } catch (e: NullPointerException) {
            throw NullPointerException("알림이 없습니다.")
        }

    }


    fun join(joinForm: JoinForm) {
        if (memberRepository.findByUsername(joinForm.username) != null) {
            throw DuplicateUsernameException("이미 가입된 id 입니다")
        }
        val member = Member(
            username = joinForm.username,
            password = passwordEncoder.encode(joinForm.password),
            name = joinForm.name,
            email = joinForm.email,
            nickname = joinForm.name
        )
        member.roles.add(USER)
        member.roles.add(STUDENT)
        memberRepository.save(member)
    }


}