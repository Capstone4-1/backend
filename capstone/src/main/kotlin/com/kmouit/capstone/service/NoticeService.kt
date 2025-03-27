package com.kmouit.capstone.service

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.Notice
import com.kmouit.capstone.repository.NoticeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service

@Transactional(readOnly = true)
class NoticeService(
    private val noticeRepository: NoticeRepository
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
}