# 🛠 Moai Backend: Troubleshooting & Security Enhancements

## 1️⃣ Lazy Loading & N+1 Problem
- **Issue:** 1:N 관계 매핑 때문에 회원 조회 시 불필요한 데이터까지 같이 조회되어 성능 저하  
- **Solution:** JPA `lazy loading` 적용으로 필요할 때만 컬렉션 조회  
- **Issue:** Lazy 로딩 컬렉션 접근 시 N+1 쿼리 발생  
- **Solution:** `fetch join`으로 한 번에 필요한 데이터 조회

## 2️⃣ Image Loading Optimization
- **Issue:** 프로필, 게시글 이미지 용량이 커서 로딩 속도 느림  
- **Solution:** 업로드 시 프론트에서 이미지를 리사이즈하여 **썸네일 전용 이미지** 생성 후 S3 저장
<img width="1261" height="1150" alt="image" src="https://github.com/user-attachments/assets/77732b4d-dd0b-4dcf-a010-4b523da8baad" />



## 3️⃣ Handling Large Data Sets
- **Issue:** 게시글/댓글/채팅 수 증가 시 전체 조회 지연  
- **Solution:**  
  - 게시글/댓글: **페이지네이션** 적용  
  - 채팅: **무한 스크롤** 구현

## 4️⃣ S3 Presigned URL for Image Upload
- **Issue:** 게시글 작성 시 이미지 업로드 어려움  
- **Solution:** `/S3/presign` API로 임시 업로드 URL 제공, 프론트에서 임시 URL로 S3 업로드 후 실제 URL 저장  

```kotlin
@GetMapping("/S3/presign")
fun responseGetPresignedUrl(
    @RequestParam filename: String,
    @RequestParam contentType: String
): Map<String, String> {
    return uploadService.getPresignedUrl(filename, contentType)
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
            builder.signatureDuration(Duration.ofMinutes(5))
                   .putObjectRequest(putObjectRequest)
        }

    val uploadUrl = presignedRequest.url().toString()
    val fileUrl = "https://${bucket}.s3.${region.id()}.amazonaws.com/$objectKey"

    return mapOf("uploadUrl" to uploadUrl, "fileUrl" to fileUrl)
}

```


## 5️⃣ Role-Based Access Control
- **Issue:** 게시글 작성 시 이미지 업로드 어려움  
- **Solution:** Spring Security + Role Hierarchy 적용 

```kotlin
@Bean
fun roleHierarchy(): RoleHierarchy {
    return RoleHierarchyImpl.fromHierarchy(
        """
        ROLE_SYSTEM > ROLE_ADMIN
        ROLE_ADMIN > ROLE_PROFESSOR
        ROLE_PROFESSOR > ROLE_MANAGER
        ROLE_MANAGER > ROLE_STUDENT_COUNCIL
        ROLE_STUDENT_COUNCIL > ROLE_STUDENT
        ROLE_STUDENT > ROLE_USER
        """.trimIndent()
    )
}
```



## 6️⃣ API Security with JWT
- **Issue:** 기본 인증만으로는 API 보안 취약, 비밀번호 암호화
- **Solution:** Spring Security + Role Hierarchy 적용
- `JWTUtil: Access/Refresh Token 생성, 만료 체크, 권한 확인
- `JwtAuthenticationFilter: 요청마다 JWT 검증 후 SecurityContext에 인증 정보 설정
- 'BCryptPasswordEncoder 빈으로 등록하여 사용 _> 비밀번호 단방향 암호화

```kotlin
@Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()


@Component
class JWTUtil(
    @Value("\${spring.jwt.secret}") secret: String,
    @Value("\${spring.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${spring.jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long
) {
    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.jcaName
    )

    fun createAccessToken(id:Long, name:String, username: String, role: List<String>, expiredMs: Long = accessTokenExpiration): String { ... }
    fun createRefreshToken(id:Long, username: String, expiredMs: Long = refreshTokenExpiration): String { ... }
    fun getUsername(token: String): String { ... }
    fun getRole(token: String): List<String> { ... }
    fun isExpired(token: String): Boolean { ... }
}

@Component
class JwtAuthenticationFilter(
    @Value("\${spring.jwt.secret}") private val secret: String,
    private val customUserDetailService: CustomUserDetailService
) : OncePerRequestFilter() {
    private val secretKey: SecretKey = SecretKeySpec(
        secret.toByteArray(StandardCharsets.UTF_8),
        SignatureAlgorithm.HS256.jcaName
    )

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) { ... }
}
```

