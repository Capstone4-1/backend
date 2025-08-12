package com.kmouit.capstone.domain.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash


@RedisHash(value = "EmailCode", timeToLive = 300)
class EmailCode (

    @Id
    val email : String,
    val code : String
){
}