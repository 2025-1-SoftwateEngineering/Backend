package com.example.vocabook.domain.member.dto.req;

public class MemberReqDTO {

    // 친구 요청 수락 or 거절
    public record UpdateFriendRequest(
            String state
    ) {}

    // 사용자 단순 조회
    public record SearchMember(
            String email
    ) {}
}
