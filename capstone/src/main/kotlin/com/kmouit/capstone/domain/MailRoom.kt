package com.kmouit.capstone.domain

import jakarta.persistence.*


@Entity
class MailRoom(
    @Id @GeneratedValue
    val id: Long? = null,

    @OneToMany(mappedBy = "mailRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mailRoomInfos: MutableList<MailRoomInfo> = mutableListOf(),

    @OneToMany(mappedBy = "mailRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mails: MutableList<Mail> = mutableListOf()
)