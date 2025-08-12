package com.kmouit.capstone.repository.redis

import com.kmouit.capstone.domain.redis.EmailCode
import org.springframework.data.repository.CrudRepository

interface EmailCodeRepository : CrudRepository<EmailCode, String> {

}