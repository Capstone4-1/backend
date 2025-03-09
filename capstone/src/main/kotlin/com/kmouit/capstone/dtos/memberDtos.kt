package com.kmouit.capstone.dtos

import jakarta.validation.constraints.NotEmpty


data class LoginForm(
    @NotEmpty
    val username : String,
    @NotEmpty
    val password :String,
)

data class JoinForm(
    @NotEmpty
    val username: String,
    @NotEmpty
    val password: String,
    @NotEmpty
    val name :String,
    @NotEmpty
    val email :String,
)