package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "lecture_room_id"])]
)
class LectureMarkInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_room_id", nullable = false)
    var lectureRoom: LectureRoom? = null,

    var targetUrl: String? = null
)
