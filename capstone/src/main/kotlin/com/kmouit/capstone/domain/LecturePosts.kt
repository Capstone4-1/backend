package com.kmouit.capstone.domain

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.LecturePostType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class LecturePosts(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "posts_id")
    var id: Long? = null,
    var title: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_room_id")
    var lectureRoom: LectureRoom? = null,
    var createdDate: LocalDateTime? = null,
    @Lob
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    var lecturePostType : LecturePostType? = null,

    var likeCount: Int = 0, // 하; 변수명 like로 했다가 삽질 2시간함; Mysql 예약어였음
    var viewCount: Int = 0,

    @Lob
    var imageUrls: String?= null,
    var thumbnailUrl : String? = null,
    var targetUrl : String? = null
    )
data class LecturePostsDto(
    val id: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val content: String?,
    val likeCount: Int,
    var lectureRoomId : Long?,
    val lecturePostType: LecturePostType,
    val writerNickname: String,
    val writerProfileImageUrl: String?,
    val writerProfileThumbnails: String?,
    val writerId: Long?,
    var imageUrls: String?= null,
    var viewCount : Int?,
    var isAuthor: Boolean
)
fun LecturePosts.toDto(currentUserId: Long?): LecturePostsDto {
    return LecturePostsDto(
        id = this.id!!,
        title = this.title ?: "",
        createdDate = this.createdDate,
        content = this.content,
        likeCount = this.likeCount,
        lecturePostType = this.lecturePostType!!,
        lectureRoomId = this.lectureRoom!!.id,
        writerNickname = this.member?.nickname ?: "탈퇴회원",
        writerProfileImageUrl = this.member?.profileImageUrl,
        writerProfileThumbnails = this.member?.thumbnailUrl,
        writerId = this.member?.id,
        imageUrls = this.imageUrls,
        viewCount = this.viewCount,
        isAuthor = (currentUserId != null && currentUserId == this.member?.id)
    )
}

