package com.kmouit.capstone.service

import com.kmouit.capstone.InquiryCategory
import com.kmouit.capstone.InquiryState
import com.kmouit.capstone.Role
import com.kmouit.capstone.api.InquiryRequest
import com.kmouit.capstone.domain.jpa.InquiryItem
import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.repository.jpa.InquiryItemRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Transactional(readOnly = true)
@Service
class InquiryService(
    private val inquiryItemRepository: InquiryItemRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional
    fun createInquiry(request: InquiryRequest, memberId: Long): InquiryItem {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("존재하지 않는 회원 입니다.") }

        val inquiry = InquiryItem(
            title = request.title,
            content = request.content,
            member = member,
            inquiryCategory = request.category,// 문의 유형 반영
            targetRole = if (request.category == InquiryCategory.ROLE_REQUEST) {
                request.targetRole // 권한 요청일 때만 설정
            } else null
        )
        inquiry.createDateTime = LocalDateTime.now()

        return inquiryItemRepository.save(inquiry)
    }

    fun getInquiries(page: Int, size: Int, state: String?): Page<InquiryDto> {
        val pageable = PageRequest.of(page, size)
        return if (state != null) {
            val inquiryState = InquiryState.from(state)
                ?: throw IllegalArgumentException("잘못된 상태값입니다. (PROCESSING / COMPLETED 만 가능)")
            inquiryItemRepository.findByInquiryState(inquiryState, pageable)
                .map { InquiryDto.from(it) }
        } else {
            inquiryItemRepository.findAll(pageable)
                .map { InquiryDto.from(it) }
        }
    }


    @Transactional
    fun completeInquiry(id: Long, answer: String, member: Member) {
        val responder = memberRepository.findById(member.id!!).orElseThrow { NoSuchElementException("존재하지않는 회원") }
        val inquiryItem = inquiryItemRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 문의") }
        inquiryItem.inquiryState = InquiryState.COMPLETED
        inquiryItem.completeDateTime = LocalDateTime.now()
        inquiryItem.reply = answer
        inquiryItem.responder = responder
    }

}

data class InquiryDto(
    val id: Long,
    val title: String,
    val content: String,
    val reply : String?,
    val targetRole: Role?,
    val userName: String?,
    val userId : Long,
    val userNickname: String?,
    val state: InquiryState, // ✅ 상태 필드 추가
    val category: InquiryCategory,
    val createdAt: LocalDateTime,
    val completeDateTime :LocalDateTime?,
    val responderId : Long? = null,
    val responderNickname :String? = null
) {
    companion object {
        fun from(entity: InquiryItem): InquiryDto {
            return InquiryDto(
                id = entity.id!!,
                title = entity.title,
                content = entity.content,
                reply = entity.reply,
                targetRole = entity.targetRole,
                userName = entity.member!!.name,
                userId = entity.member!!.id!!,
                userNickname = entity.member!!.nickname,
                category = entity.inquiryCategory,
                state = entity.inquiryState,
                createdAt = entity.createDateTime!!,
                completeDateTime = entity.completeDateTime,
                responderId = entity.responder?.id,
                responderNickname = entity.responder?.nickname
            )
        }
    }
}