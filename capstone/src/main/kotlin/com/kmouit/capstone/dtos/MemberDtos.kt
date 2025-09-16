package com.kmouit.capstone.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.kmouit.capstone.domain.jpa.Member
import com.kmouit.capstone.domain.jpa.Notice
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime


data class LoginForm @JsonCreator constructor(
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String,
)

data class JoinForm(
    @NotEmpty
    var username: String,
    @NotEmpty
    var password: String,
    @NotEmpty
    var name: String,
    @NotEmpty
    var email: String,
    @NotEmpty
    var nickname: String,
)

data class MemberSimpleDto(
    var id: Long,
    var nickName: String,
    var profileImageUrl: String?,
    var profileThumbnails: String?,
    val intro: String?,
) {
    constructor(member: Member) : this(
        id = member.id!!,
        nickName = member.nickname!!,
        profileImageUrl = member.profileImageUrl,
        profileThumbnails = member.thumbnailUrl,
        intro = member.intro
    )
}

data class MeDto(
    var id: Long,
    var username: String,
    var name: String,
    var email: String,
    var nickname: String,
    var roles: List<String>,
    var intro: String? = null,
    var profileImageUrl: String? = null,
    var profileThumbnails: String? = null
) {
    constructor(member: Member) : this(
        id = member.id!!,
        username = member.username!!,
        name = member.name!!,
        email = member.email!!,
        nickname = member.nickname!!,
        roles = member.roles.map { it.name },
        intro = member.intro,
        profileImageUrl = member.profileImageUrl,
        profileThumbnails = member.thumbnailUrl
    )
}


data class NoticeDto(
    var id: Long,
    var content: String?,
    var date: LocalDateTime,
    var targetUrl: String?,
) {
    constructor(notice: Notice) : this(
        id = notice.id!!,
        content = notice.content,
        date = notice.date!!,
        targetUrl = notice.targetUrl
    )
}

data class MemberDto(
    var id: Long,
    var username: String,
    var name: String,
    var email: String,
    var nickname: String,
    var roles: List<String>,
    var intro: String? = null,
    var profileImageUrl: String? = null,
    var profileThumbnails : String? = null
) {
    constructor(member: Member) : this(
        id = member.id!!,
        username = member.username!!,
        name = member.name!!,
        email = member.email!!,
        nickname = member.nickname!!,
        roles = member.roles.map { it.name },
        intro = member.intro,
        profileImageUrl = member.profileImageUrl,
        profileThumbnails = member.thumbnailUrl
    )
}


data class IntroRequest(
    val intro: String,
)