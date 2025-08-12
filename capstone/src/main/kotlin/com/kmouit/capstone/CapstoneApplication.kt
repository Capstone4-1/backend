package com.kmouit.capstone

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.kmouit.capstone.repository.jpa"])
@EnableRedisRepositories(basePackages = ["com.kmouit.capstone.repository.redis"])
class CapstoneApplication{

}

fun main(args: Array<String>) {
	val context = runApplication<CapstoneApplication>(*args)
}
