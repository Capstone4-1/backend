package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime


@Entity
class CrawledNotice(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var content: String? = null,
    var date: LocalDate? = null,


    var imageUrls: String?= null
) {


}