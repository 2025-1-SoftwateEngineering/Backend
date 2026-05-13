package com.example.vocabook.domain.member.dto.req;

import com.example.vocabook.domain.member.enums.ReportReason;
import jakarta.annotation.Nullable;

public class MemberReqDTO {

    // 친구 요청 수락 or 거절
    public record UpdateFriendRequest(
            String state
    ) {}

    // 사용자 단순 조회
    public record SearchMember(
            String email
    ) {}

    // 사용자 신고
    public record ReportMember(
            ReportReason reportReason,
            @Nullable
            String detailReason
    ) {}
}
