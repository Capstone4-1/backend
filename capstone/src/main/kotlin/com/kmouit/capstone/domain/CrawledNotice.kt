package com.kmouit.capstone.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime


class CrawledNotice(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id :Long? = null,
    val title : String? = null,
    val content : String? = null,
    val date: LocalDateTime? = null
) {


}