package com.kmouit.capstone.service

import com.kmouit.capstone.api.RoleRequestDto
import com.kmouit.capstone.domain.jpa.RequestStatus
import com.kmouit.capstone.domain.jpa.RoleRequest
import com.kmouit.capstone.repository.jpa.MemberRepository
import com.kmouit.capstone.repository.jpa.RoleRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class RoleRequestService(
    private val memberRepository: MemberRepository,
    private val roleRequestRepository: RoleRequestRepository
) {
    @Transactional
    fun createRoleRequest(memberId: Long, dto: RoleRequestDto): RoleRequest {
        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("회원이 존재하지 않습니다.") }

        val roleRequest = RoleRequest(
            member = member,
            requestedRole = dto.requestedRole,
            reason = dto.reason,
            requestDate = LocalDateTime.now()
        )

        return roleRequestRepository.save(roleRequest)
    }

    @Transactional
    fun approveRoleRequest(requestId: Long): RoleRequest {
        val request = roleRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("권한 요청이 존재하지 않습니다.") }

        request.status = RequestStatus.APPROVED
        request.responseDate = LocalDateTime.now()

        // 실제 member_roles 테이블에 권한 추가 로직 필요
        // 예: member.addRole(request.requestedRole)

        return request
    }

    @Transactional
    fun rejectRoleRequest(requestId: Long, reason: String?): RoleRequest {
        val request = roleRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("권한 요청이 존재하지 않습니다.") }

        request.status = RequestStatus.REJECTED
        request.responseDate = LocalDateTime.now()
        request.reason = reason

        return request
    }
}