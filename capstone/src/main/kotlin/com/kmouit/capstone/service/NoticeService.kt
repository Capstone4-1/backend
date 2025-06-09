package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.NoticeInfoStatus.READ
import com.kmouit.capstone.NoticeType
import com.kmouit.capstone.NoticeType.NEW_COMMENT
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.Notice
import com.kmouit.capstone.domain.Posts
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
    fun createCommentNotice(post: Posts, member: Member) {
        val boardType = post.boardType!!.name.lowercase() // FREE → "free"
        val postUrl = "/main/community/$boardType/post/${post.id}"

        var newNickName = member.nickname
        if (post.boardType == BoardType.SECRET){
            newNickName = "익명"
        }
        val notice = Notice().apply {
            this.date = LocalDateTime.now()
            this.content = "'${post.title}' 게시글에 '${newNickName}'님이 댓글을 남겼습니다"
            this.noticeType = NEW_COMMENT
            this.targetUrl = postUrl
            this.member = post.member
        }
        post.member!!.addNotice(notice)

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

    @Transactional
    fun createLecturePostNotice(professor: Member, postTitle: String, lectureRoomId: Long, postId: Long) {
        val postUrl = "/main/study-dashboard/$lectureRoomId/$postId"

        val notice = Notice().apply {
            this.date = LocalDateTime.now()
            this.content = "'$postTitle' 글이 새로 작성되었습니다."
            this.noticeType = NoticeType.NEW_LECTURE_POST
            this.targetUrl = postUrl
            this.member = professor
        }

        professor.addNotice(notice)
    }



}