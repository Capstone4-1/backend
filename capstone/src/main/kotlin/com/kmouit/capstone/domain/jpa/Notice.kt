package com.kmouit.capstone.domain.jpa

import com.kmouit.capstone.NoticeInfoStatus
import com.kmouit.capstone.NoticeType
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Notice (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member : Member?  = null,
    var content : String? = null,
    var date : LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var status : NoticeInfoStatus = NoticeInfoStatus.UNREAD,


    var targetUrl: String? = null,

    @Enumerated(EnumType.STRING)
    var noticeType : NoticeType? = null
)