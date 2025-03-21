package com.kmouit.capstone.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.kmouit.capstone.domain.Member
import jakarta.validation.constraints.NotEmpty


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

data class MemberDto(
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