package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*


@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["member_id", "posts_id"])
    ]
)
class PostLikeInfo (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? =null,

    @ManyToOne
    @JoinColumn(name = "member_id")
    var member : Member? = null,

    @ManyToOne
    @JoinColumn(name = "posts_id")
    var posts: Posts? = null
)


