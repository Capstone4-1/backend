package com.kmouit.capstone.service

import com.kmouit.capstone.api.MenuDayDTO
import com.kmouit.capstone.domain.jpa.*
import com.kmouit.capstone.repository.jpa.MenuItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MenuService(
    private val menuItemRepository: MenuItemRepository
) {

    @Transactional
    fun saveCrawledMenu(menuWeek: List<MenuDayDTO>) {
        val menuItems = mutableListOf<MenuItem>()
        menuWeek.forEach { itemOfDay ->
            // 학생식당
            itemOfDay.studentCafeteria?.forEach { (cornerType, names) ->
                names.forEach { name ->
                    if (!menuItemRepository.existsByNameAndDateAndCafeteriaTypeAndCornerType(
                            name,
                            itemOfDay.date,
                            CafeteriaType.STUDENT,
                            cornerType
                        )
                    ) {
                        menuItems += MenuItem(
                            name = name,
                            date = itemOfDay.date,
                            cafeteriaType = CafeteriaType.STUDENT,
                            cornerType = cornerType
                        )
                    }
                }
            }

            itemOfDay.staffCafeteria?.forEach { (mealType, names) ->
                names.forEach { name ->
                    if (!menuItemRepository.existsByNameAndDateAndCafeteriaTypeAndMealType(
                            name,
                            itemOfDay.date,
                            CafeteriaType.STAFF,
                            mealType
                        )
                    ) {
                        menuItems += MenuItem(
                            name = name,
                            date = itemOfDay.date,
                            cafeteriaType = CafeteriaType.STAFF,
                            mealType = mealType
                        )
                    }
                }
            }
        }

        if (menuItems.isNotEmpty()) {
            menuItemRepository.saveAll(menuItems)
            println("저장완료: ${menuItems.size}개 항목")
        } else {
            println("저장할 신규 메뉴가 없습니다.")
        }
    }

    fun getDailyMenu(year: Int, month: Int, day: Int): MenuDayDTO {
        val date = LocalDate.of(year, month, day)
        val items = menuItemRepository.findByDate(date)

        val studentMenu = items
            .filter { it.cafeteriaType == CafeteriaType.STUDENT }
            .groupBy { it.cornerType!! }
            .mapValues { entry -> entry.value.map { it.name ?: "" } }
            .ifEmpty { null }

        val staffMenu = items
            .filter { it.cafeteriaType == CafeteriaType.STAFF }
            .groupBy { it.mealType!! }
            .mapValues { entry -> entry.value.map { it.name ?: "" } }
            .ifEmpty { null }

        return MenuDayDTO(
            date = date,
            studentCafeteria = studentMenu,
            staffCafeteria = staffMenu
        )
    }
}

data class MenuDayDTO(
    val date: LocalDate,
    val studentCafeteria: Map<CornerType, List<String>>? = null,
    val staffCafeteria: Map<MealType, List<String>>? = null
)
