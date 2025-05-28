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
    SECRET("SECRET");
    companion object {
        fun from(value: String): BoardType? =
            entries.find { it.value == value.uppercase() }
    }
}

enum class CheckListItemStatus(val value: String) {
    DONE("DONE"),
    PENDING("PENDING");

    companion object {
        fun from(value: String): CheckListItemStatus? =
            CheckListItemStatus.entries.find { it.value == value.uppercase() }
    }
}