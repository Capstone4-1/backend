package com.kmouit.capstone.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import java.net.URLEncoder
import java.time.Duration
import java.util.*

@Service
class UploadService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}") private val secretKey: String,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,

) {
    private val region = Region.AP_NORTHEAST_2
    private val credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
    )

    private val s3Presigner: S3Presigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build()
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
        return "https://${bucket}.s3.${region.id()}.amazonaws.com/$fileName"
    }


    //todo 테스트 안해봄

    fun getPresignedUrl(filename: String): Map<String, String> {
        val encodedFilename = URLEncoder.encode(filename, Charsets.UTF_8).replace("+", "%20")
        val objectKey = "uploads/$encodedFilename"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType("image/*")
            .build()

        val presignedRequest: PresignedPutObjectRequest =
            s3Presigner.presignPutObject { builder ->
                builder
                    .signatureDuration(Duration.ofMinutes(5))
                    .putObjectRequest(putObjectRequest)
            }

        val uploadUrl = presignedRequest.url().toString()
        val fileUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$objectKey"

        return mapOf(
            "uploadUrl" to uploadUrl,
            "fileUrl" to fileUrl
        )
    }




}
