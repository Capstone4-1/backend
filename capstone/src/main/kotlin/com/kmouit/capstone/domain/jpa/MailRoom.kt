package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*


@Entity
class MailRoom(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToMany(mappedBy = "mailRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mailRoomInfos: MutableList<MailRoomInfo> = mutableListOf(),

    @OneToMany(mappedBy = "mailRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val mails: MutableList<Mail> = mutableListOf()
)