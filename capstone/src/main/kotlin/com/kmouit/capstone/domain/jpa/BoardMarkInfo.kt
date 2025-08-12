package com.kmouit.capstone.domain.jpa

import com.kmouit.capstone.BoardType
import jakarta.persistence.*


@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "boardType"])]
)
class BoardMarkInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Enumerated(EnumType.STRING)
    var boardType: BoardType? = null,

    var boardName: String? = null,
    var targetUrl: String? = null
)

data class BoardMarkInfoDto(
    val id: Long,
    val boardType: BoardType,
    val boardName: String,
    val targetUrl: String
)
fun BoardMarkInfo.toDto(): BoardMarkInfoDto {
    return BoardMarkInfoDto(
        id = this.id!!,
        boardType = this.boardType!!,
        boardName = this.boardName ?: "",
        targetUrl = this.targetUrl ?: ""
    )
}