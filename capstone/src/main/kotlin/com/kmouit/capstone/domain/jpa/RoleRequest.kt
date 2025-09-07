package com.kmouit.capstone.domain.jpa

import com.kmouit.capstone.Role
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class RoleRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false)
    val requestedRole: Role,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RequestStatus = RequestStatus.PENDING,

    @Column(name = "request_date", nullable = false)
    val requestDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "response_date")
    var responseDate: LocalDateTime? = null,

    @Column(name = "reason", length = 500)
    var reason: String? = null, // 요청 사유
)

enum class RequestStatus {
    PENDING, APPROVED, REJECTED
}