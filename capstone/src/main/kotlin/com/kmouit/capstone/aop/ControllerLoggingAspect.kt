package com.kmouit.capstone.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Aspect
@Component
class ControllerLoggingAspect {

    private val log = LoggerFactory.getLogger(this::class.java)

    // RestController 어노테이션 붙은 클래스 내부 메서드 모두 대상
    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    fun logBefore(joinPoint: JoinPoint) {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name
        val args = joinPoint.args.joinToString(", ") { it?.toString() ?: "null" }

        log.info("컨트롤러 호출: $className.$methodName($args)")
    }
}