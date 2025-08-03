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

data class ScheduleInfoDto(
    val dow: DayOfWeek,
    val startTime: Int,
    val endTime: Int
) {
    companion object {
        fun from(entity: ScheduleInfo): ScheduleInfoDto {
            return ScheduleInfoDto(
                dow = entity.dow ?: DayOfWeek.MONDAY,
                startTime = entity.startTime ?: 0,
                endTime = entity.endTime ?: 0
            )
        }
    }
}