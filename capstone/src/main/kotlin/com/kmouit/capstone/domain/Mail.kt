package com.kmouit.capstone.domain
import com.kmouit.capstone.MailStatus
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Mail(
    @Id @GeneratedValue
    @Column(name = "mail_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id") // 수정
    var sender: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id") // 수정
    var receiver: Member? = null,

    var content:String? = null,

    var date :LocalDateTime?  = null,

    @Enumerated(EnumType.STRING)
    var status: MailStatus = MailStatus.NEW
)