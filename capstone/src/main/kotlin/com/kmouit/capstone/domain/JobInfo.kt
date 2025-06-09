package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job_info")
data class JobInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val company: String,

    @Column(nullable = false)
    val region: String,

    @Column(name = "employment_type", nullable = false)
    val employmentType: String,

    @Column(columnDefinition = "TEXT")
    val description: String,

    @Column(name = "target_url", nullable = false)
    val targetUrl: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class JobInfoDto(
    val id: Long,
    val title: String,
    val company: String,
    val region: String,
    val employmentType: String,
    val description: String,
    val targetUrl: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: JobInfo): JobInfoDto {
            return JobInfoDto(
                id = entity.id!!,
                title = entity.title,
                company = entity.company,
                region = entity.region,
                employmentType = entity.employmentType,
                description = entity.description,
                targetUrl = entity.targetUrl,
                createdAt = entity.createdAt
            )
        }
    }
}
