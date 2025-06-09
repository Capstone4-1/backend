package com.kmouit.capstone.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Comments (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var content : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val parent: Comments? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL])
    val replies: MutableList<Comments> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member : Member? = null,
    var createdDate : LocalDateTime? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Posts? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_post_id")
    var lecturePost: LecturePosts? = null,
    var likeCount : Int = 0
)

data class CommentDto(
    var id: Long,
    var content: String,
    var writerId: Long,
    var writerNickname: String,
    var writerProfileImageUrl: String?,
    val writerProfileThumbnails: String?,
    var createdDate: LocalDateTime,
    var likeCount: Int,
    var isAuthor: Boolean,
    var hasChildren: Boolean //
)

fun Comments.toDto(currentUserId: Long?): CommentDto {
    return CommentDto(
        id = this.id ?: throw IllegalStateException("댓글 ID가 null입니다."),
        content = this.content.orEmpty(),
        writerId = this.member?.id ?: -1,
        writerNickname = this.member?.nickname ?: "탈퇴회원",
        writerProfileImageUrl = this.member?.profileImageUrl,
        writerProfileThumbnails = this.member?.thumbnailUrl,
        createdDate = this.createdDate ?: LocalDateTime.now(),
        likeCount = this.likeCount,
        isAuthor = (currentUserId != null && currentUserId == this.member?.id),
        hasChildren = this.replies.isNotEmpty()
    )
}