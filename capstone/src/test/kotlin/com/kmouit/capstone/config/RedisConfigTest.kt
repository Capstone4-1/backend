package com.kmouit.capstone.config

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import kotlin.test.Test


@SpringBootTest
@Import(RedisConfig::class)
class RedisConfigTest {
    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, Any>
    @Test
    fun redisTemplateString() {
        val ops = redisTemplate.opsForValue()
        val key = "name"
        ops.set(key, "giraffe")
        val value = ops.get(key)
        Assertions.assertEquals("giraffe", value)
    }
}