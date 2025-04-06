package com.kmouit.capstone.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class UploadService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String
) {
    fun uploadProfileImage(file: MultipartFile): String {
        val allowedTypes = listOf("image/jpeg", "image/png", "image/webp", "image/gif")  // 허용할 이미지 타입
        if (!allowedTypes.contains(file.contentType)) {
            throw IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.")
        }
        val fileName = "profile-images/${UUID.randomUUID()}-${file.originalFilename}"
        val putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.contentType)
            .build()
        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.inputStream, file.size))
        return "https://${bucket}.s3.ap-northeast-2.amazonaws.com/$fileName"
    }


}
