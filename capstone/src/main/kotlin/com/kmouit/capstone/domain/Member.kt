package com.kmouit.capstone.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.kmouit.capstone.Role
import jakarta.annotation.Nullable
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
class Member(
    @Id @GeneratedValue
    @Column(name = "member_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    @NotNull
    var username: String? = null,  //학번임

    @Column(nullable = false)
    @NotNull
    var password: String? = null,

    @NotNull
    @Column(nullable = false)
    var name: String? = null,
    @NotNull
    @Column(nullable = false, unique = true)
    var email: String? = null,
    @NotNull
    @Column(nullable = false)
    var nickname: String? = null,

    var profileImageUrl: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(),

    @Column(length = 200)
    @Nullable
    var intro: String? = null,


    @JsonIgnore
    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true
    )
    var notices: MutableList<Notice> = mutableListOf(),
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
