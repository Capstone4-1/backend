package com.kmouit.capstone.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl


@Configuration
class MailConfig {

    @Bean
    fun mailSender() : JavaMailSender {
        val mailSender  = JavaMailSenderImpl();
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587
        mailSender.username ="moai37487@gmail.com"
        mailSender.password = "ipivyshhiiidodrd"

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp";
        props["mail.smtp.auth"] = "true";
        props["mail.smtp.starttls.enable"] = "true";
        props["mail.debug"] = "true";
        return mailSender;
    }
}