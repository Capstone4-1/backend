package com.kmouit.capstone.api

import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("search-users")
    fun responseSearchUserByAdmin(
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) role: String?
    ): ResponseEntity<List<MemberSummaryDto>> {

        val members = adminService.searchUsers(username, name, role)
        val result = members.map { MemberSummaryDto.from(it) }

        return ResponseEntity.ok(result)
    }
}

data class MemberSummaryDto(
    val id: Long,
    val username : String,
    val name: String,
    val email: String,
    val role: String
) {
    companion object {
        fun from(member: Member): MemberSummaryDto {
            return MemberSummaryDto(
                id = member.id!!,
                username = member.username ?: "",
                name = member.name ?: "",
                email = member.email ?: "",
                role = member.roles.firstOrNull()?.name ?: "N/A"
            )
        }
    }
}