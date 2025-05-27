package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Comments (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val content : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member : Member? = null,

    val createdDate : LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    val post: Posts? = null,

    val likeCount : Int = 0
)