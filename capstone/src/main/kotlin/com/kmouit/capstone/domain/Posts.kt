package com.kmouit.capstone.domain

import com.kmouit.capstone.BoardType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Posts(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "posts_id")
    var id: Long? = null,
    var title: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,
    var createdDate: LocalDateTime? = null,
    @Lob
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    var boardType: BoardType? = null,

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var comments: MutableList<Comments> = mutableListOf(),
    var commentCount: Int = 0,
    var likeCount: Int = 0, // 하; 변수명 like로 했다가 삽질 2시간함; Mysql 예약어였음
    var viewCount: Int = 0,

    @Lob
    var imageUrls: String?= null,
    var price : Int? = null,
    )
data class PostDto(
    val id: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val content: String?,
    val commentCount: Int,
    val likeCount: Int,
    val boardType: BoardType,
    val writerNickname: String,
    val writerProfileImageUrl: String?,
    val writerId: Long?,
    var imageUrls: String?= null,
    val comments: List<CommentDto>,
    var price : Int?,
    var viewCount : Int?,
    var isAuthor: Boolean
)
fun Posts.toDto(currentUserId: Long?): PostDto {
    return PostDto(
        id = this.id!!,
        title = this.title ?: "",
        createdDate = this.createdDate,
        content = this.content,
        commentCount = this.commentCount,
        likeCount = this.likeCount,
        boardType = this.boardType!!,
        writerNickname = this.member?.nickname ?: "탈퇴회원",
        writerProfileImageUrl = this.member?.profileImageUrl,
        writerId = this.member?.id,
        imageUrls = this.imageUrls,
        comments = this.comments.map { it.toDto(currentUserId) },
        price = this.price,
        viewCount = this.viewCount,
        isAuthor = (currentUserId != null && currentUserId == this.member?.id)
    )
}

data class SimplePostDto(
    val id: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val commentCount: Int,
    val likeCount: Int,
    val boardType: BoardType,
    val writerNickname: String,
    val price : Int?,
    val imageUrls: String?,
    val viewCount: Int
)
fun Posts.toSimpleDto(): SimplePostDto {
    return SimplePostDto(
        id = this.id!!,
        title = this.title!!,
        createdDate = this.createdDate,
        commentCount = this.commentCount,
        likeCount = this.likeCount,
        boardType = this.boardType!!,
        writerNickname = this.member?.nickname!!,
        price = this.price,
        imageUrls = this.imageUrls,
        viewCount = this.viewCount
    )
}