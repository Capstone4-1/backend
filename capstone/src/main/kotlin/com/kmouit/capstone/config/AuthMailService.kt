package com.kmouit.capstone.config

import com.kmouit.capstone.domain.redis.EmailCode
import com.kmouit.capstone.repository.redis.EmailCodeRepository
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthMailService(
    private val javaMailSender: JavaMailSender,
    private val emailCodeRepository: EmailCodeRepository,
) {
    companion object {
        private const val senderEmail = "moai37487@gmail.com"
    }

    // 랜덤 인증번호 생성
    fun createNumber(): String {
        val random = Random()
        val key = StringBuilder()

        repeat(6) { // 무조건 6자리
            when (random.nextInt(2)) { // 0: 대문자, 1: 숫자
                0 -> key.append((random.nextInt(26) + 65).toChar()) // 대문자
                1 -> key.append(random.nextInt(10)) // 숫자
            }
        }
        return key.toString()
    }

    @Throws(MessagingException::class)
    fun createMail(mail: String, number: String): MimeMessage {
        val message = javaMailSender.createMimeMessage()

        message.setFrom(senderEmail)
        message.setRecipients(Message.RecipientType.TO, mail)
        message.subject = "이메일 인증"
        val body = buildString {
            append("<h3>요청하신 인증 번호입니다.</h3>")
            append("<h1>$number</h1>")
            append("<h3>감사합니다.</h3>")
        }
        message.setText(body, "UTF-8", "html")

        return message
    }

    // 메일 발송
    @Throws(MessagingException::class)
    fun sendSimpleMessage(sendEmail: String): String {
        val number = createNumber()
        val message = createMail(sendEmail, number)
        try {
            javaMailSender.send(message)
            emailCodeRepository.save(EmailCode(sendEmail, number))
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("메일 발송 중 오류가 발생했습니다.")
        }
        return number
    }

    // 코드 검증
    fun verifyCode(email: String, code: String): Boolean {
        val savedCode = emailCodeRepository.findById(email).map { it.code }.orElse(null)
        val isValid = savedCode != null && savedCode == code

        if (isValid) {
            emailCodeRepository.deleteById(email)
        }
        return isValid
    }
}
