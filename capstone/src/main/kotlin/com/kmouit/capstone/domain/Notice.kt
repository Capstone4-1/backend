package com.kmouit.capstone.domain

import com.kmouit.capstone.NoticeInfoStatus
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Notice (
    @Id  @GeneratedValue
    var id : Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member : Member?  = null,
    var content : String? = null,
    var date : LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var status : NoticeInfoStatus = NoticeInfoStatus.UNREAD
)