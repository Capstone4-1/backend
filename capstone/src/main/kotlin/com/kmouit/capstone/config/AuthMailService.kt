package com.kmouit.capstone.config

import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthMailService(
    private val javaMailSender: JavaMailSender
) {
    companion object {
        private const val senderEmail = "moai37487@gmail.com"
        private val verificationCodes = ConcurrentHashMap<String, String>()
    }

    // 랜덤 인증번호 생성
    fun createNumber(): String {
        val random = Random()
        val key = StringBuilder()

        repeat(8) {
            when (random.nextInt(3)) {
                0 -> key.append((random.nextInt(26) + 97).toChar()) // 소문자
                1 -> key.append((random.nextInt(26) + 65).toChar()) // 대문자
                2 -> key.append(random.nextInt(10)) // 숫자
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
            verificationCodes[sendEmail] = number
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("메일 발송 중 오류가 발생했습니다.")
        }
        return number
    }

    // 코드 검증
    fun verifyCode(email: String, code: String): Boolean {
        val savedCode = verificationCodes[email]
        val isValid = savedCode != null && savedCode == code

        if (isValid) {
            verificationCodes.remove(email)
        }
        return isValid
    }
}
