package com.kmouit.capstone.domain

import com.kmouit.capstone.CheckListItemStatus
import jakarta.persistence.*

class CheckListItem(

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    var content : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Enumerated(EnumType.STRING)
    val status : CheckListItemStatus
) {
}