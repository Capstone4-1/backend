package com.kmouit.capstone

enum class Role(val value:String) {
    USER("USER"),
    STUDENT("STUDENT"),
    PROFESSOR("PROFESSOR"),
    STUDENT_COUNCIL("STUDENT_COUNCIL"),
    MANAGER("MANAGER"),
    ADMIN("ADMIN"),
    SYSTEM("SYSTEM");

    companion object {
        fun from(value: String): Role? =
            Role.entries.find { it.value == value.uppercase() }
    }
}
enum class InquiryState(val value: String) {
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED");
    companion object {
        fun from(value: String): InquiryState? =
            entries.find { it.value == value }
    }
}

enum class InquiryCategory(val value: String) {
    ROLE_REQUEST("ROLE_REQUEST"), //권한
    ACCOUNT_ISSUE("ACCOUNT_ISSUE"), //계정 이슈
    GENERAL("GENERAL"),
    REPORT("REPORT"); //신고

    companion object {
        fun from(value: String): InquiryCategory? =
            entries.find { it.value == value }
    }
}
enum class NoticeInfoStatus{
    UNREAD, READ
}

enum class MailStatus{
    NEW, OLD
}

enum class BoardType(val value: String) {
    FREE("FREE"),
    NOTICE("NOTICE"),
    POPULAR("POPULAR"),
    MARKET("MARKET"),
    SECRET("SECRET"),
    REVIEW("REVIEW"),
    QNA("QNA"),
    NOTICE_UNIV("NOTICE_UNIV"),
    NOTICE_DEPT("NOTICE_DEPT"),
    NOTICE_SC("NOTICE_SC"),

    //lecture 관련 타입
    LECTURE_Q("LECTURE_Q"), //질문
    LECTURE_N("LECTURE_N"),  //공지
    LECTURE_REF("LECTURE_REF"),
    LECTURE_R("LECTURE_R"); //후기

    companion object {
        fun from(value: String): BoardType? =
            entries.find { it.value == value.uppercase() }
    }
}
enum class LecturePostType(val value: String) {
    //lecture 관련 타입
    LECTURE_Q("LECTURE_Q"), //질문
    LECTURE_N("LECTURE_N"),  //공지
    LECTURE_REF("LECTURE_REF"),
    LECTURE_R("LECTURE_R"); //후기

    companion object {
        fun from(value: String): LecturePostType? =
            entries.find { it.value == value.uppercase() }
    }
}


enum class NoticeType(val value : String?){
    NEW_COMMENT("NEW_COMMENT"),
    NEW_LECTURE_POST("NEW_LECTURE_POST")
}

enum class TodoItemStatus(val value: String) {
    DONE("DONE"),
    PENDING("PENDING");

    companion object {
        fun from(value: String): TodoItemStatus? =
            TodoItemStatus.entries.find { it.value == value.uppercase() }
    }
}

enum class VerificationResult {
    SUCCESS,
    INVALID_CODE,
    EXPIRED
}