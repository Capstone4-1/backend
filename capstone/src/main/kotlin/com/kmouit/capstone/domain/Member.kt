package com.kmouit.capstone.domain

import com.kmouit.capstone.Role
import jakarta.persistence.*

@Entity
class Member(
    @Id @GeneratedValue
    @Column(name = "member_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String? = null,  //학번임

    @Column(nullable = false)
    var password: String? = null,

    var name: String? = null,
    var email: String? = null,
    var nickname: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(),


    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notices: MutableList<Notice> = mutableListOf()
) {
    fun addNotice(notice: Notice) {
        notice.member = this
        notices.add(notice)
    }
    fun removeNotice(notice: Notice) {
        notice.member = null
        notices.remove(notice)
    }


}
