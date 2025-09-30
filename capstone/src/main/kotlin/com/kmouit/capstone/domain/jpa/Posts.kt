package com.kmouit.capstone.domain.jpa

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
    var likeCount: Int = 0, // 하; 변수명 like로 했다가 삽질 2시간함; Mysql 예약어였음
    var viewCount: Int = 0,

    @Lob
    var imageUrls: String?= null,
    var thumbnailUrl : String? = null,
    var price : Int? = null,
    var targetUrl : String? = null
    )
data class PostDto(
    val id: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val content: String?,
    val likeCount: Int,
    val boardType: BoardType,
    val writerNickname: String,
    val writerProfileImageUrl: String?,
    val writerProfileThumbnails: String?,
    val writerId: Long?,
    var imageUrls: String?= null,
    var price: Int?,
    var viewCount: Int?,
    var isAuthor: Boolean,
    var isLike: Boolean? = null,
    var isScrapped : Boolean?= null,
    var targetUrl: String? = null
)
fun Posts.toDto(currentUserId: Long?, isLike : Boolean = false, isScrapped: Boolean = false): PostDto {
    val isSecretBoard = this.boardType == BoardType.SECRET
    return PostDto(
        id = this.id!!,
        title = this.title ?: "",
        createdDate = this.createdDate,
        content = this.content,
        likeCount = this.likeCount,
        boardType = this.boardType!!,
        writerNickname = if (isSecretBoard) "익명" else this.member?.nickname ?: "탈퇴회원",
        writerProfileImageUrl = if (isSecretBoard) null else this.member?.profileImageUrl,
        writerProfileThumbnails = if (isSecretBoard) null else this.member?.thumbnailUrl,
        writerId = if (isSecretBoard) null else this.member?.id,
        imageUrls = this.imageUrls,
        price = this.price,
        viewCount = this.viewCount,
        isAuthor = (currentUserId != null && currentUserId == this.member?.id),
        isLike = isLike,
        isScrapped = isScrapped,
        targetUrl = this.targetUrl
    )
}
data class SimplePostDto(
    val id: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val commentCount: Long?,
    val likeCount: Int,
    val boardType: BoardType,
    val writerNickname: String,
    val price : Int?,
    val imageUrls: String?,
    val thumbNailUrl : String?,
    val viewCount: Int,
    val isAuthor: Boolean
)
fun Posts.toSimpleDto(currentUserId: Long?, commentCount: Long?): SimplePostDto {
    val isSecretBoard = this.boardType == BoardType.SECRET

    return SimplePostDto(
        id = this.id!!,
        title = this.title!!,
        createdDate = this.createdDate,
        commentCount = commentCount,
        likeCount = this.likeCount,
        boardType = this.boardType!!,
        writerNickname = if (isSecretBoard) "익명" else this.member?.nickname ?: "탈퇴회원",
        price = this.price,
        imageUrls = this.imageUrls,
        thumbNailUrl = this.thumbnailUrl,
        viewCount = this.viewCount,
        isAuthor = (currentUserId != null && currentUserId == this.member?.id)
    )
}