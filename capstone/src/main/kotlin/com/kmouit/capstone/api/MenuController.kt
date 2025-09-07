package com.kmouit.capstone.api

import com.kmouit.capstone.domain.jpa.MenuItem
import com.kmouit.capstone.service.MenuService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("/api/Menu")
class MenuController(
    private val menuService: MenuService
) {
    @GetMapping("/{year}/{month}/{day}")
    fun getDailyMenu(
        @PathVariable year: Int,
        @PathVariable month: Int,
        @PathVariable day: Int
    ): ResponseEntity<Map<String, Any>> {
        val dailyMenu = menuService.getDailyMenu(year, month, day)

        return ResponseEntity.ok(
            mapOf(
                "message" to "하루치 메뉴 조회 성공",
                "data" to dailyMenu
            )
        )
    }
}