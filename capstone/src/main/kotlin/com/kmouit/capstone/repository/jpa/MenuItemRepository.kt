package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.CafeteriaType
import com.kmouit.capstone.domain.jpa.CornerType
import com.kmouit.capstone.domain.jpa.MealType
import com.kmouit.capstone.domain.jpa.MenuItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
interface MenuItemRepository :JpaRepository<MenuItem, Long>{

    fun existsByNameAndDateAndCafeteriaTypeAndCornerType(
        name: String,
        date: LocalDate?,
        cafeteriaType: CafeteriaType,
        cornerType: CornerType
    ): Boolean

    // 교직원식당 중복 체크
    fun existsByNameAndDateAndCafeteriaTypeAndMealType(
        name: String,
        date: LocalDate?,
        cafeteriaType: CafeteriaType,
        mealType: MealType
    ): Boolean
}