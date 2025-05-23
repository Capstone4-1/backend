package com.kmouit.capstone.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime


class Comment (
    val id: Long? = null,

    val content : String? = null,

    val createdBy : Member? = null,

    val createdDate : LocalDateTime? = null,

    val post: Post? = null,

    val like : Int = 0
)