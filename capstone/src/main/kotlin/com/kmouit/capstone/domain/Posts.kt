package com.kmouit.capstone.domain

import com.kmouit.capstone.BoardType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Posts(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "posts_id")
    var id: Long? = null,
    var title: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    var createdDate: LocalDateTime? = null,

    @Lob
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    var boardType: BoardType? = null,

    //var comments:ArrayList<Comment> = arrayListOf(),

    var commentCount : Int = 0,

    var likeCount: Int = 0 // 하; 변수명 like로 했다가 삽질 2시간함; Mysql 예약어였음.

    )