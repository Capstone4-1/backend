package com.kmouit.capstone.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
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