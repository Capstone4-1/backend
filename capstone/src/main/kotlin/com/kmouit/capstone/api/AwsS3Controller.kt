package com.kmouit.capstone.api
import com.kmouit.capstone.service.S3UploadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/aws")
class AwsS3Controller(
    private val uploadService: S3UploadService
) {
    /**
     * 이미지 업로드를 위한 임시 업로드 presignUrl 발급 api
     */
    @GetMapping("/S3/presign")
    fun responseGetPresignedUrl(@RequestParam filename : String ): Map<String, String> {
        return uploadService.getPresignedUrl(filename)
    }
}