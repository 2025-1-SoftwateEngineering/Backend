package com.example.vocabook.domain.member.dto.res;

import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MemberResDTO {

    // 내 프로필 조회
    @Builder
    public record MyProfile(
            String nickname,
            String email,
            Long streak,
            Long totalStudyDays,
            Long coin,
            Authorize authorize,
            List<Image> images
    ) {}

    @Builder
    public record Image(
            String imageUrl,
            ItemType itemType
    ){}

    // 친구 요청 목록 조회
    @Builder
    public record FriendRequestList(
            Long id,
            Long fromMemberId,
            String nickname,
            FriendState state
    ) {}

    // 친구 요청 보내기
    @Builder
    public record SendFriendRequest(
            Long id,
            String nickname
    ) {}

    // 친구 요청 토글 (수락 or 거절)
    @Builder
    public record UpdateFriendRequest(
            Long id,
            String nickname,
            FriendState state
    ) {}

    // 사용자 단순 조회
    @Builder
    public record SearchMember(
            Long id,
            String nickname,
            String email
    ) {}

    // 친구 목록 조회
    @Builder
    public record FriendList(
            Long toMemberId,
            FriendState state
    ) {}

    // 친구 프로필 조회
    @Builder
    public record FriendProfile(
            Long id,
            String nickname,
            Integer totalWordsLearned,
            Long streak,
            Long totalStudyDays,
            Long coin,
            LocalDateTime loginAt
    ) {}

    // 친구 차단
    @Builder
    public record Blocking(
            Long id,
            String nickname,
            LocalDateTime blockedAt
    ) {}

    // 사용자 신고
    @Builder
    public record ReportMember(
            Long id,
            String email,
            LocalDateTime reportedAt
    ) {}

    // 프로필 수정
    @Builder
    public record UpdateProfile(
            String email,
            String nickname
    ) {}

    // 친구 삭제
    @Builder
    public record DeleteFriend(
            Long id,
            String nickname,
            String email,
            LocalDateTime deletedAt
    ) {}
}
