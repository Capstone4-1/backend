package com.kmouit.capstone.repository.redis

import com.kmouit.capstone.domain.redis.RefreshToken
import org.springframework.data.repository.CrudRepository

interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {

}