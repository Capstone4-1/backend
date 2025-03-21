package com.kmouit.capstone.dtos

import com.kmouit.capstone.domain.Member


data class RequestMemberDtoList(
    var requestMember: List<MemberDto>,
)

data class FriendRequestDto(
    var idToDecline: Long
)

data class AcceptFriendRequestDto(
    var idToAccept: Long
)