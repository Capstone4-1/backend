package com.kmouit.capstone.domain

import com.kmouit.capstone.Role
import jakarta.persistence.*

@Entity
class Member(

    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String? = null  ,

    @Column(nullable = false)
    var password: String? = null ,

    var name: String? = null,
    var email: String? = null,
    var nickname: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = mutableSetOf()

)  {

}
