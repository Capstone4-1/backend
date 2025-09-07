package com.kmouit.capstone.domain.jpa

import com.kmouit.capstone.InquiryCategory
import com.kmouit.capstone.InquiryState
import com.kmouit.capstone.Role
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class InquiryItem(
    var title: String,
    var content: String,

    var reply : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Enumerated(EnumType.STRING)
    var inquiryState: InquiryState = InquiryState.PROCESSING,

    @Enumerated(EnumType.STRING)
    var inquiryCategory: InquiryCategory = InquiryCategory.GENERAL,

    @Enumerated(EnumType.STRING)
    var targetRole :Role? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var createDateTime: LocalDateTime? = null

    var completeDateTime: LocalDateTime? = null

    @PrePersist
    fun onCreate() {
        this.createDateTime = LocalDateTime.now()
    }
}