package com.kmouit.capstone.api

import com.kmouit.capstone.Role
import com.kmouit.capstone.domain.jpa.RequestStatus
import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.RoleRequestService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/role-requests")
class RoleRequestController(
    private val roleRequestService: RoleRequestService
) {

    @PostMapping
    fun createRoleRequest(
        @RequestBody dto: RoleRequestDto,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RoleRequestResponseDto> {
        val request = roleRequestService.createRoleRequest(userDetails.member.id!!, dto)
        return ResponseEntity.ok(
            RoleRequestResponseDto(
                id = request.id,
                requestedRole = request.requestedRole,
                status = request.status,
                reason = request.reason,
                requestDate = request.requestDate,
                responseDate = request.responseDate
            )
        )
    }

    @PostMapping("/{id}/approve")
    fun approveRoleRequest(@PathVariable id: Long): ResponseEntity<String> {
        roleRequestService.approveRoleRequest(id)
        return ResponseEntity.ok("권한 요청 승인 완료")
    }

    @PostMapping("/{id}/reject")
    fun rejectRoleRequest(
        @PathVariable id: Long,
        @RequestBody rejectDto: Map<String, String>
    ): ResponseEntity<String> {
        val reason = rejectDto["reason"]
        roleRequestService.rejectRoleRequest(id, reason)
        return ResponseEntity.ok("권한 요청 거절 완료")
    }
}

data class RoleRequestDto(
    val requestedRole: Role,
    val reason: String? = null
)


data class RoleRequestResponseDto(
    val id: Long,
    val requestedRole: Role,
    val status: RequestStatus,
    val reason: String?,
    val requestDate: LocalDateTime,
    val responseDate: LocalDateTime?
)