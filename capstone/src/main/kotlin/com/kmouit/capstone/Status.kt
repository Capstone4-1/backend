package com.kmouit.capstone

enum class Role(val value:String) {
    USER("USER"),
    STUDENT("STUDENT"),
    MANAGER("MANAGER"),
    PROFESSOR("PROFESSOR"),
    ADMIN("ADMIN"),
    SYSTEM("SYSTEM");

    companion object {
        fun from(value: String): Role? =
            Role.entries.find { it.value == value.uppercase() }
    }
}
enum class InquiryState(val value: String) {
    PROCESSING("처리중"),
    COMPLETED("완료");
    companion object {
        fun from(value: String): InquiryState? =
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
    NOTICE_C("NOTICE_C"),

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