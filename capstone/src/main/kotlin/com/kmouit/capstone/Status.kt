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

    LECTURE_Q("LECTURE_Q"),
    LECTURE_NOTICE("LECTURE_NOTICE"),
    LECTURE_REF("LECTURE_REF");


    companion object {
        fun from(value: String): BoardType? =
            entries.find { it.value == value.uppercase() }
    }
}

enum class NoticeType(val value : String?){
    NEW_COMMENT("NEW_COMMENT"),
}

enum class TodoItemStatus(val value: String) {
    DONE("DONE"),
    PENDING("PENDING");

    companion object {
        fun from(value: String): TodoItemStatus? =
            TodoItemStatus.entries.find { it.value == value.uppercase() }
    }
}