package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class LectureRoom (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    var title : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var createBy :Member? = null,

    val grade : Int? = null,
    val semester : Int? = null,

    val intro : String? = null,
    var createdDate : LocalDate? = null,
    var code : String? = null,

)