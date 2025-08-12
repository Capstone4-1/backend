package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*
import java.io.Serializable


@Entity
class MailRoomInfo(

    @EmbeddedId
    val id: MailRoomInfoId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mailRoomId") // 복합키의 필드와 연결
    @JoinColumn(name = "mail_room_id")
    val mailRoom: MailRoom,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    val member: Member
)

@Embeddable
data class MailRoomInfoId(
    val mailRoomId: Long = 0,
    val memberId: Long = 0
) : Serializable