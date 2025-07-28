package com.kmouit.capstone.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.kmouit.capstone.Role
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    @NotNull
    var username: String? = null,

    @Column(nullable = false)
    @NotNull
    var password: String? = null,

    @Column(nullable = false)
    @NotNull
    var name: String? = null,

    @Column(nullable = false, unique = true)
    @NotNull
    var email: String? = null,

    @Column(nullable = false, unique = true, length = 10) // ✅ 최대 10자 제한
    @NotNull
    @jakarta.validation.constraints.Pattern(
        regexp = "^[a-zA-Z0-9가-힣]{1,13}$", // ✅ 공백 및 특수문자 불가 정규식
        message = "닉네임은 1~13자의 한글, 영어, 숫자만 가능합니다. 공백과 특수문자는 사용할 수 없습니다."
    )
    var nickname: String? = null,

    var profileImageUrl: String? = null,
    var thumbnailUrl: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "member_roles",
        joinColumns = [JoinColumn(name = "member_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(),

    @Column(length = 200) // ✅ DB 제약
    @jakarta.validation.constraints.Size(
        max = 200,
        message = "자기소개는 200자 이내로 작성해주세요."
    )
    var intro: String? = null,

    @JsonIgnore
    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true
    )
    var notices: MutableList<Notice> = mutableListOf(),
) {
    fun addNotice(notice: Notice) {
        notice.member = this
        notices.add(notice)
    }

    fun removeNotice(notice: Notice) {
        notice.member = null
        notices.remove(notice)
    }
}


data class FriendSummaryDto(
    var id: Long,
    var username: String,
    var name: String,
    var email: String,
    var nickname: String,
    var roles: List<String>,
    var intro: String? = null,
    var profileImageUrl: String? = null,
    var profileThumbnails : String? = null,
    val isFriend: Boolean // 🔽 친구 여부 추가
) {
    constructor(member: Member, isFriend: Boolean) : this(
        id = member.id!!,
        username = member.username!!,
        name = member.name!!,
        email = member.email!!,
        nickname = member.nickname!!,
        roles = member.roles.map { it.name },
        intro = member.intro,
        profileImageUrl = member.profileImageUrl,
        profileThumbnails = member.thumbnailUrl,
        isFriend = isFriend
    )
}

