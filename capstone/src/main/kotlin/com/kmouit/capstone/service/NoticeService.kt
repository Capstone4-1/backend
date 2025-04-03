package com.kmouit.capstone.service

import com.kmouit.capstone.NoticeInfoStatus.READ
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.Notice
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.NoticeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service

@Transactional(readOnly = true)
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val memberRepository: MemberRepository
) {


    /**
     * (알림 받을사람, 내용)
     */
    @Transactional
    fun createNotice(member: Member, content: String) {
        val notice = Notice().apply {
            this.date = LocalDateTime.now()
            this.content = content
        }
        member.addNotice(notice)
        noticeRepository.save(notice)
    }


    @Transactional
    fun deleteNotice(id :Long){
        noticeRepository.deleteById(id)
    }


    @Transactional
    fun readNotice(id: Long) {
        val notice = noticeRepository.findById(id).orElseThrow { NoSuchElementException("존재하지 않는 알림") }
        notice.status = READ
    }


    @Transactional
    fun readAllNotice(noticeIds :List<Long>){
        val notices = noticeRepository.findAllById(noticeIds)
        notices.forEach { it.status = READ }
    }


}