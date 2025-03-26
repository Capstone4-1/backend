package com.kmouit.capstone.dtos


data class RequestMemberDtoList(
    var requestMember: List<MemberSimpleDto>,
)

data class FriendRequestDto(
    var idToDecline: Long
)

data class AcceptFriendRequestDto(
    var idToAccept: Long
)