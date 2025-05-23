package com.kmouit.capstone.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime


class Post(
    val id: Long? = null,

    val title : String? = null,

    val createdBy : Member? = null,

    val createdDate : LocalDateTime? = null,

    val content : String? = null,

    val board : Board? = null,

    val comments :ArrayList<Comment> = arrayListOf(),

    val like : Int = 0


    )