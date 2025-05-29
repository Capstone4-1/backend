package com.kmouit.capstone.domain

import com.kmouit.capstone.CheckListItemStatus
import jakarta.persistence.*


@Entity
class Todo(

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    var content : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Enumerated(EnumType.STRING)
    val status : CheckListItemStatus
) {
}

data class TodoDto(
    val id: Long,
    val content: String,
    val status: CheckListItemStatus,
) {
    constructor(todo: Todo) : this(
        id = todo.id ?: -1L,
        content = todo.content ?: "",
        status = todo.status,
    )
}