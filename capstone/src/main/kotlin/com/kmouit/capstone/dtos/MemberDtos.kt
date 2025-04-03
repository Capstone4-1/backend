package com.kmouit.capstone.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.Notice
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime


data class LoginForm @JsonCreator constructor(
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String
)
data class JoinForm(
    @NotEmpty
    var username: String,
    @NotEmpty
    var password: String,
    @NotEmpty
    var name :String,
    @NotEmpty
    var email :String,
)

data class MemberSimpleDto(
    var id :Long,
    var name :String,
    var username :String,
){
    constructor(member: Member) : this(
        id = member.id!!,
        username = member.username!!,
        name = member.name!!
    )
}


data class NoticeDto(
    var content : String,
    var date: LocalDateTime
){
    constructor(notice: Notice) : this(
        content = notice.content!!,
        date = notice.date!!,
    )
}

data class MemberDto(
    var id:Long,
    var username:String,
    var name:String,
    var email: String,
    var nickname:String,
    var roles: List<String>,
    var intro: String? = null
){
    constructor(member: Member) : this(
        id = member.id!!,
        username = member.username!!,
        name = member.name!!,
        email = member.email!!,
        nickname = member.nickname!!,
        roles = member.roles.map { it.name },
        intro = member.intro)
}

data class IntroRequest(
    val intro: String
)