package com.kmouit.capstone.service

import com.kmouit.capstone.api.InquiryRequest
import com.kmouit.capstone.domain.jpa.InquiryItem
import com.kmouit.capstone.repository.jpa.InquiryItemRepository
import com.kmouit.capstone.repository.jpa.MemberRepository
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
        val member = memberRepository.findById(memberId).orElseThrow { NoSuchElementException("존재하지 않는 회원 입니다.") }
        val inquiry = InquiryItem(
            title = request.title,
            content = request.content,
            member = member
        )
        inquiry.createDateTime = LocalDateTime.now()
        return inquiryItemRepository.save(inquiry)
    }
}