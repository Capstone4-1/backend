package com.kmouit.capstone.domain.jpa

import jakarta.persistence.*
import java.time.LocalDate


@Entity
@Table(name = "site_manage_info", uniqueConstraints = [UniqueConstraint(columnNames = ["date"])])
class SiteManageInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val date: LocalDate,

    // 방문자 수 (중복 방문자 포함, 예: 페이지뷰)
    var totalVisits: Long = 0,

    // 방문자 수 (중복 제거, 유니크 방문자 수)
    var uniqueVisitors: Long = 0,

    // 오늘 작성된 게시글 수
    var postCount: Long = 0,

    // 오늘 작성된 댓글 수
    var commentCount: Long = 0
)