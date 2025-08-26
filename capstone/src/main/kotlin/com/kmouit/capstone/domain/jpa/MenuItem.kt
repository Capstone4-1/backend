package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*
import java.time.LocalDate



@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["cafeteriaType", "mealType", "cornerType", "name", "date"]
        )
    ]
)
class MenuItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // 학생식당인지 교직원식당인지
    @Enumerated(EnumType.STRING)
    var cafeteriaType: CafeteriaType? = null,
    // 교직원식당에서만 사용
    @Enumerated(EnumType.STRING)
    var mealType: MealType? = null,
    // 학생식당에서만 사용
    @Enumerated(EnumType.STRING)
    var cornerType: CornerType? = null,
    var name: String? = null,
    var date: LocalDate? = null
)
enum class CafeteriaType {
    STUDENT, STAFF
}
enum class MealType {
    BREAKFAST, LUNCH, DINNER
}
enum class CornerType {
    WESTERN, RAMEN, SNACK, SET_MENU
}
