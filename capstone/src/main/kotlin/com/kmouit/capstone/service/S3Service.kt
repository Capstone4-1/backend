package com.kmouit.capstone.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.nio.file.Files
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String
) {
    fun uploadFile(file: MultipartFile): String {
        val fileName = "uploads/${UUID.randomUUID()}-${file.originalFilename}"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.inputStream, file.size))
        return "https://${bucket}.s3.ap-northeast-2.amazonaws.com/$fileName"
    }

    fun uploadLocalFile(file: File): String {
        val fileName = "uploads/${UUID.randomUUID()}-${file.name}"
        val contentType = Files.probeContentType(file.toPath()) ?: "application/octet-stream"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromFile(file))

        return "https://${bucket}.s3.ap-northeast-2.amazonaws.com/$fileName"
    }
}
