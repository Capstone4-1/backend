package com.kmouit.capstone

import com.kmouit.capstone.service.S3Service
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class CapstoneApplication

fun main(args: Array<String>) {
	val context = runApplication<CapstoneApplication>(*args)

	val s3Service = context.getBean(S3Service::class.java)

	val file = File("C:\\Users\\user\\OneDrive\\바탕 화면\\pexels-pixabay-33545.jpg")
	val imageUrl = s3Service.uploadLocalFile(file)
	println("imageUrl = ${imageUrl}")
	println("✅ 업로드 성공! 이미지 URL: $imageUrl")

}
