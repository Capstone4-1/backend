package com.kmouit.capstone.domain.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash


@RedisHash(value = "RefreshToken", timeToLive = 60 * 60 * 24 * 7)
class RefreshToken (

    @Id
    val username :String,
    val token :String
)