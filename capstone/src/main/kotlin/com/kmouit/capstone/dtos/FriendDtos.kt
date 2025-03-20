package com.kmouit.capstone.dtos

import com.kmouit.capstone.domain.Member


data class RequestMemberDtoList(
    var requestMember: List<RequestMemberDto>,
)

data class RequestMemberDto(
    var id: Long? = null,
    var username: String? = null,
    var name: String? = null,
) {

    /**
     * Member-> Dto 변환
     */
    constructor(member: Member) : this(
        id = member.id,
        username = member.username,
        name = member.name
    )

}