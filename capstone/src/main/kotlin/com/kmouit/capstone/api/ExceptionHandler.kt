package com.kmouit.capstone.api

import com.kmouit.capstone.dtos.ErrorResponse
import com.kmouit.capstone.exception.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(e: NullPointerException): ResponseEntity<ErrorResponse> {
        logger.warn { "해당 항목이 존재하지 않습니다: $e" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), e.message ?: "요청한 데이터를 찾을 수 없습니다"))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): ResponseEntity<ErrorResponse> {
        logger.warn { "리소스를 찾을 수 없음: $e" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), e.message ?: "요청한 데이터를 찾을 수 없습니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error { "서버 오류 발생: $e" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message ?: "알 수 없는 에러"))
    }

    @ExceptionHandler(DuplicateUsernameException::class)
    fun handleDuplicateUsernameException(e: DuplicateUsernameException): ResponseEntity<ErrorResponse> {
        logger.warn { "회원가입 회원 중복: $e" }
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), e.message ?: "중복 에러"))
    }

    @ExceptionHandler(DuplicateMailRoomException::class)
    fun handleDuplicateMailRoomException(e: DuplicateMailRoomException): ResponseEntity<ErrorResponse> {
        logger.warn { "채팅방 생성 중복: $e" }
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), e.message ?: "중복 에러"))
    }

    @ExceptionHandler(CustomAccessDeniedException::class)
    fun handleAccessDenied(e: CustomAccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn { "잘못된 접근 (권한 없음): $e" }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), e.message ?: "권한 에러"))
    }

    @ExceptionHandler(FileSizeLimitExceededException::class)
    fun handleFileSizeLimitExceeded(e: FileSizeLimitExceededException): ResponseEntity<ErrorResponse> {
        logger.warn { "업로드 파일 크기 초과: ${e.message}" }
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), e.message ?: "파일 크기 초과"))
    }
}

// 커스텀 예외 클래스
class DuplicateMailRoomException(message: String) : RuntimeException(message)
