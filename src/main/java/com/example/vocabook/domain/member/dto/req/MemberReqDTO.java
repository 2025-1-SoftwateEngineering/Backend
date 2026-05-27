package com.example.vocabook.domain.member.dto.req;

import com.example.vocabook.domain.member.enums.ReportReason;
import com.example.vocabook.domain.store.enums.ItemType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

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

    // 프로필 수정
    public record UpdateProfile(
            String nickname,
            String email,
            List<ProfileList> updateProfileList,
            @NotBlank(message = "기존 비밀번호는 필수 입력입니다.")
            String confirmPassword
    ) {}

    public record ProfileList(
            ItemType itemType,
            Long targetId
    ){}
}
