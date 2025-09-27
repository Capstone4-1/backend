# Moai 백엔드


-게시글 썸네일에 사진이 많을시 가져오는데 너무 느렸음. -> 첫 업로드시 resize로 썸네일용 이미지를 만들고 s3에 저장후 사용
-게시글, 댓글, 채팅 수가 많아질수록 가져오는데 오래 걸렸음. -> 페이지네이션 으로 한페이지씩 가져옴, 채팅은 무한스크롤 구현하면서 사용


## ERD
![image](https://github.com/user-attachments/assets/2cf550e4-df68-45cc-b5cd-0f7d88839ed4)



