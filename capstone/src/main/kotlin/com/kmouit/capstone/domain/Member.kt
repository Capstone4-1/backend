package com.kmouit.capstone.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


@Entity
class Member(

    @Id @GeneratedValue
    var id :Long? = null,
    var username :String? = null,
    var password :String? = null,
    var name :String? = null,
    var email :String? = null,
    var nickname :String? = null,
    var roles :String? = null
){
    fun getRoleList(): List<String> {
        if(this.roles?.length!! > 0){
            return this.roles!!.split(",")
        }
        return listOf()
    }
}