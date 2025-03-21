package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.io.Serializable


@Entity
class FriendInfo(
    @EmbeddedId
    var friendInfoId: FriendInfoId? = null,

    @Enumerated(EnumType.STRING)
    var status :FriendStatus = FriendStatus.SENDING  //기본값은 요청중
)

@Embeddable
data class FriendInfoId(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_member_id")
    val sendMember:Member, //보낸 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receive_member_id")
    val receiveMember:Member  // 받는 사람
): Serializable

enum class FriendStatus{
    SENDING, ACCEPTED
}