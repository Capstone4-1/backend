package com.kmouit.capstone.api

import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


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

    @PostMapping("/grant-role")
    fun grantRole(
        @RequestParam userId: Long,
        @RequestParam role: String
    ): ResponseEntity<String> {
        adminService.grantRole(userId, role)
        return ResponseEntity.ok("권한 부여 성공")
    }

    @DeleteMapping("/revoke-role")
    fun revokeRole(
        @RequestParam userId: Long,
        @RequestParam role: String
    ): ResponseEntity<String> {
        adminService.revokeRole(userId, role)
        return ResponseEntity.ok("권한 회수 성공")
    }
}

data class MemberSummaryDto(
    val id: Long,
    val username: String,
    val nickname: String,
    val name: String,
    val email: String,
    val roles: List<String>,
) {
    companion object {
        fun from(member: Member): MemberSummaryDto {
            return MemberSummaryDto(
                id = member.id!!,
                username = member.username ?: "",
                nickname = member.nickname!!,
                name = member.name ?: "",
                email = member.email ?: "",
                roles = member.roles.map { it.name },
            )
        }
    }
}
