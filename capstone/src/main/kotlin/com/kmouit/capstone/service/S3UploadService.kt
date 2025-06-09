package com.kmouit.capstone.service

import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.Duration
import java.util.*
import javax.imageio.ImageIO

@Service
class S3UploadService(
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

    fun uploadWithThumbnail(file: MultipartFile): Pair<String, String> {
        val allowedTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/gif" to "gif"
            // WebP는 기본 ImageIO에서 미지원 (추가 의존성 필요)
        )

        val contentType = file.contentType ?: throw IllegalArgumentException("파일의 contentType을 알 수 없습니다.")
        val extension = allowedTypes[contentType]
            ?: throw IllegalArgumentException("지원하지 않는 이미지 형식입니다: $contentType")

        val uuid = UUID.randomUUID().toString()

        // 원본 업로드
        val originalKey = "original-images/${uuid}.$extension"
        val originalRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(originalKey)
            .contentType(contentType)
            .build()
        s3Client.putObject(originalRequest, RequestBody.fromInputStream(file.inputStream, file.size))

        // 썸네일 생성
        val thumbnailKey = "thumbnails/${uuid}.$extension"
        val thumbnailImage = Thumbnails.of(file.inputStream)
            .size(200, 200)
            .outputFormat(extension)
            .asBufferedImage()

        val baos = ByteArrayOutputStream()
        ImageIO.write(thumbnailImage, extension, baos)
        baos.flush()

        val thumbnailRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(thumbnailKey)
            .contentType(contentType)
            .build()
        s3Client.putObject(thumbnailRequest, RequestBody.fromBytes(baos.toByteArray()))
        baos.close()

        val originalUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$originalKey"
        val thumbnailUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$thumbnailKey"

        return originalUrl to thumbnailUrl
    }


    fun getPresignedUrl(filename: String, contentType: String): Map<String, String> {
        val uuid = UUID.randomUUID()
        val extension = filename.substringAfterLast('.', "jpg")
        val objectKey = "original-images/$uuid.$extension"

        val safeContentType = contentType.ifBlank { "application/octet-stream" }

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(safeContentType)
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

    fun generateThumbnailFromOriginalUrl(originalUrl: String): String {
        val objKey = originalUrl.substringAfter("amazonaws.com/")

        val s3Object = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(objKey)
                .build()
        )

        // ✅ InputStream 직접 사용
        val bufferedImage = ImageIO.read(s3Object)
            ?: throw IllegalArgumentException("❌ S3 이미지 스트림을 읽을 수 없습니다. URL: $originalUrl")

        val baos = ByteArrayOutputStream()

        Thumbnails.of(bufferedImage) // ✅ vararg 필요 없음
            .size(200, 200)
            .outputFormat("jpg")
            .toOutputStream(baos)

        val uuid = UUID.randomUUID().toString()
        val thumbnailKey = "thumbnails/$uuid.jpg"

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(thumbnailKey)
                .contentType("image/jpeg")
                .build(),
            RequestBody.fromBytes(baos.toByteArray())
        )

        return "https://${bucket}.s3.${region.id()}.amazonaws.com/$thumbnailKey"
    }


    fun uploadExternalImageAndGenerateThumbnail(externalImageUrl: String): Pair<String?, String?> {
        if (externalImageUrl.lowercase().endsWith(".webp")) {
            println("WebP 형식은 건너뜀: $externalImageUrl")
            return null to null
        }

        val url = URL(externalImageUrl)
        val originalImage = ImageIO.read(url)
            ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다: $externalImageUrl")

        // ✅ 알파 채널 제거 및 RGB 변환
        val rgbImage = BufferedImage(originalImage.width, originalImage.height, BufferedImage.TYPE_INT_RGB)
        val g = rgbImage.createGraphics()
        g.drawImage(originalImage, 0, 0, Color.WHITE, null) // 배경을 흰색으로 채움
        g.dispose()

        val uuid = UUID.randomUUID().toString()
        val extension = "jpg"

        // ✅ 원본 저장 (JPG 변환된)
        val originalKey = "original-images/crawling-image/$uuid.$extension"
        val originalBaos = ByteArrayOutputStream()
        ImageIO.write(rgbImage, extension, originalBaos)
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(originalKey)
                .contentType("image/jpeg")
                .build(),
            RequestBody.fromBytes(originalBaos.toByteArray())
        )

        // ✅ 썸네일 생성
        val thumbnailImage = Thumbnails.of(rgbImage)
            .size(200, 200)
            .outputFormat(extension)
            .asBufferedImage()

        val thumbnailBaos = ByteArrayOutputStream()
        ImageIO.write(thumbnailImage, extension, thumbnailBaos)

        val thumbnailKey = "thumbnails/crawling-image/$uuid.$extension"
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(thumbnailKey)
                .contentType("image/jpeg")
                .build(),
            RequestBody.fromBytes(thumbnailBaos.toByteArray())
        )

        val originalUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$originalKey"
        val thumbnailUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$thumbnailKey"

        return originalUrl to thumbnailUrl
    }




    fun extractS3KeyFromUrl(url: String): String {
        return url.substringAfter("amazonaws.com/")
    }

    // 🔹 S3 객체 삭제
    fun deleteS3Object(key: String) {
        try {
            s3Client.deleteObject {
                it.bucket(bucket).key(key)
            }
            println("✅ S3 삭제 완료: $key")
        } catch (e: Exception) {
            println("❌ S3 삭제 실패: $key, ${e.message}")
        }
    }

    // 🔹 이미지/썸네일 전부 삭제
    fun deleteAllImages(imageUrls: String?, thumbnailUrl: String?) {
        // 1. 이미지 URL 여러 개 있을 경우 (쉼표 구분)
        imageUrls?.split(",")?.map { it.trim() }?.forEach { url ->
            if (url.isNotBlank()) deleteS3Object(extractS3KeyFromUrl(url))
        }

        if (!thumbnailUrl.isNullOrBlank()) {
            deleteS3Object(extractS3KeyFromUrl(thumbnailUrl))
        }
    }
}
