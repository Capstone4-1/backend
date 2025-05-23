package com.kmouit.capstone.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

class Board (
    val id : Long? = null,

    @Column(name = "board_name")
    val name : Long? = null,

    val posts :ArrayList<Post> = arrayListOf()


)