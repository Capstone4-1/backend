package com.kmouit.capstone.domain

import com.kmouit.capstone.Role
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
class Member(
    @Id @GeneratedValue
    @Column(name = "member_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String? = null ,  //학번임

    @Column(nullable = false)
    var password: String? = null ,

    var name: String? = null,
    var email: String? = null,
    var nickname: String? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = mutableSetOf()
)  {


}
