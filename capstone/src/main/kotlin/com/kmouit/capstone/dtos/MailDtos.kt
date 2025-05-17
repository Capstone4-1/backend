package com.kmouit.capstone.dtos

import com.kmouit.capstone.MailStatus
import java.time.LocalDateTime

data class MailDto(
    var id :Long ? = null,
    var receiverId : Long? = null,
    var senderId : Long? = null,
    var mailRoomId :Long ? = null,
    var content: String? = null,
    var date : LocalDateTime? = null,
    var status: MailStatus? = null
)