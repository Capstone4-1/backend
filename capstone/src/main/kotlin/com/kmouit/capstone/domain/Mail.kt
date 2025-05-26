package com.kmouit.capstone.domain
import com.kmouit.capstone.MailStatus
import com.kmouit.capstone.dtos.MailDto
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Mail(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_room_id")
    val mailRoom: MailRoom? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id") // 수정
    var sender: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id") // 수정
    var receiver: Member? = null,

    var content:String? = null,

    var date :LocalDateTime?  = null,

    @Enumerated(EnumType.STRING) //상대가 읽었나?
    var status: MailStatus = MailStatus.NEW
)


fun Mail.toDto(): MailDto {
    return MailDto(
        id = this.id,
        receiverId = this.receiver?.id,
        senderId = this.sender?.id,
        mailRoomId = this.mailRoom?.id,
        content = this.content,
        date = this.date,
        status = this.status
    )
}