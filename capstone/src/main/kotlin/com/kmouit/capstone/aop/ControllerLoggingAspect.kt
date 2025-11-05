package com.kmouit.capstone.aop

import com.kmouit.capstone.api.CrawledNoticeDto
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Aspect
@Component
class ControllerLoggingAspect {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    fun logBefore(joinPoint: JoinPoint) {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name

        val args = joinPoint.args.joinToString(", ") { arg ->
            when (arg) {
                is CrawledNoticeDto -> {
                    "CrawledNoticeDto(title=${arg.title}, date=${arg.date}, url=${arg.url}, img=${arg.img}, contentLength=${arg.content?.length ?: 0})"
                }
                is Collection<*> -> arg.joinToString(", ") { item ->
                    when (item) {
                        is CrawledNoticeDto -> "CrawledNoticeDto(title=${item.title}, contentLength=${item.content?.length ?: 0})"
                        else -> item?.toString() ?: "null"
                    }
                }
                else -> arg?.toString() ?: "null"
            }
        }

        log.info("call: $className.$methodName($args)")
    }
}
