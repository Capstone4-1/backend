package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDate


@Entity
class LectureBoard (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,
    val boardName :String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null,
    val createDate : LocalDate? = null,

    val grade : Int? = null,
    val semester : Int? = null,

    val intro : String? = null,
    val code : String? = null,
)