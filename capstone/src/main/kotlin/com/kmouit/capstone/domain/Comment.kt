package com.kmouit.capstone.domain

import java.time.LocalDateTime


class Comment (
    val id: Long? = null,

    val content : String? = null,

    val createdBy : Member? = null,

    val createdDate : LocalDateTime? = null,

    val post: Posts? = null,

    val like : Int = 0
)