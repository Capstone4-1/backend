package com.kmouit.capstone.domain.jpa

import com.kmouit.capstone.TodoItemStatus
import jakarta.persistence.*
import java.time.LocalDate


@Entity
class Todo(

    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    var content : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    var dueDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var status : TodoItemStatus
) {
}

data class TodoDto(
    val id: Long,
    val content: String,
    val status: TodoItemStatus,
    val dueDate: LocalDate?,
) {
    constructor(todo: Todo) : this(
        id = todo.id ?: -1L,
        content = todo.content ?: "",
        status = todo.status,
        dueDate = todo.dueDate!!
    )
}