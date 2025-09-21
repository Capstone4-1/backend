package com.kmouit.capstone.service

import com.kmouit.capstone.api.MenuDayDTO
import com.kmouit.capstone.domain.jpa.*
import com.kmouit.capstone.repository.jpa.MenuItemRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MenuService(
    private val menuItemRepository: MenuItemRepository,
    private val entityManager: EntityManager   // 🔑 flush/clear 용
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

            // 교직원식당
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
            batchSave(menuItems, batchSize = 50) // ✅ 배치 저장 적용
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

    /**
     * 배치 저장 (flush + clear)
     */
    private fun batchSave(entities: List<MenuItem>, batchSize: Int) {
        entities.chunked(batchSize).forEach { chunk ->
            menuItemRepository.saveAll(chunk)
            entityManager.flush()   // SQL 즉시 실행
            entityManager.clear()   // 영속성 컨텍스트 비우기 → 메모리 누수 방지
        }
    }
}

data class MenuDayDTO(
    val date: LocalDate,
    val studentCafeteria: Map<CornerType, List<String>>? = null,
    val staffCafeteria: Map<MealType, List<String>>? = null
)
