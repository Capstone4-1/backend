# 인공지능 공학부 커뮤니티

## 프로젝트 소개


## 프로젝트 구상
- **주요 기능**: 
    - **[기능 1]**: 기능에 대한 설명.
    - **[기능 2]**: 기능에 대한 설명.
    - **[기능 3]**: 기능에 대한 설명.
- **기술적 접근**: 백엔드는 **[언어, 프레임워크]**를 사용하여 구현됩니다. 데이터베이스는 **[DB 종류]**를 사용하며, **[기타 기술]**도 활용합니다.

## 기술 스택
- **프로그래밍 언어**: Java, Kotlin
- **프레임워크**: Spring Boot, 
- **데이터베이스**: MySQL,h2
- **인증/보안**: JWT,Spring Security 등
- **기타 도구 및 라이브러리**:
    - **[도구 1]**: 설명.
    - **[도구 2]**: 설명.
    - **[도구 3]**: 설명.

# API
## Member
 <details><summary>회원가입
 </summary>
     {
  "message": "회원 가입 success"
}
 </details> 

  <details><summary>접기/펼치기
 </summary>
     접은 내용(ex 소스 코드)
 </details> 

  <details><summary>접기/펼치기
 </summary>
     접은 내용(ex 소스 코드)
 </details> 

  <details><summary>접기/펼치기
 </summary>
     접은 내용(ex 소스 코드)
 </details> 

-게시글 썸네일에 사진이 많을시 가져오는데 너무 느렸음. -> 첫 업로드시 resize로 썸네일용 이미지를 만들고 s3에 저장후 사용
-게시글, 댓글, 채팅 수가 많아질수록 가져오는데 오래 걸렸음. -> 페이지네이션 으로 한페이지씩 가져옴, 채팅은 무한스크롤 구현하면서 사용


## ERD
![image](https://github.com/user-attachments/assets/2cf550e4-df68-45cc-b5cd-0f7d88839ed4)



