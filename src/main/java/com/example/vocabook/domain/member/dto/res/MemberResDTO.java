package com.example.vocabook.domain.member.dto.res;

import com.example.vocabook.domain.member.enums.FriendState;
import lombok.Builder;

import java.time.LocalDateTime;

public class MemberResDTO {

    // 내 프로필 조회
    @Builder
    public record MyProfile(
            String nickname,
            String email,
            Long streak,
            Long coin
    ) {}

    // 친구 요청 목록 조회
    @Builder
    public record FriendRequestList(
            Long id,
            Long fromMemberId,
            String nickname,
            FriendState state
    ){}

    // 친구 요청 보내기
    @Builder
    public record SendFriendRequest(
            Long id,
            String nickname
    ){}

    // 친구 요청 토글 (수락 or 거절)
    @Builder
    public record UpdateFriendRequest(
            Long id,
            String nickname,
            FriendState state
    ){}

    // 사용자 단순 조회
    @Builder
    public record SearchMember(
            Long id,
            String nickname,
            String email
    ){}

    // 친구 목록 조회
    @Builder
    public record FriendList(
            Long toMemberId,
            FriendState state
    ){}

    // 친구 프로필 조회
    @Builder
    public record FriendProfile(
            Long id,
            String nickname,
            Long streak,
            Long coin,
            LocalDateTime loginAt
    ){}

    // 친구 차단
    @Builder
    public record Blocking(
            Long id,
            String nickname,
            LocalDateTime blockedAt
    ){}
}
