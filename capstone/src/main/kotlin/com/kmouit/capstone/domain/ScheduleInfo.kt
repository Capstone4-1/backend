package com.kmouit.capstone.domain
import jakarta.persistence.*
import java.time.DayOfWeek

@Entity
class ScheduleInfo (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lectureRoom_id")
    var lectureRoom: LectureRoom? = null,

    @Enumerated(EnumType.STRING)
    var dow : DayOfWeek? = null,

    var startTime : Int? = null,

    var endTime : Int? = null
)