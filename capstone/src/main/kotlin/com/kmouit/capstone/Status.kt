package com.kmouit.capstone

enum class Role {
    USER, STUDENT, MANAGER, PROFESSOR, ADMIN;
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
    SECRET("SECRET");
    companion object {
        fun from(value: String): BoardType? =
            entries.find { it.value == value.uppercase() }
    }
}