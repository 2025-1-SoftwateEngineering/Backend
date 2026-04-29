package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;

import java.time.LocalDateTime;

public class MemberConverter {

    // 회원가입 (Member)
    public static Member toMember(
            AuthReqDTO.SignUp dto,
            String saltedPassword
    ) {
        return Member.builder()
                .email(dto.email())
                .password(saltedPassword)
                .nickname(dto.nickname())
                .build();
    }

    // 내 프로필 조회
    public static MemberResDTO.MyProfile toMyProfile(
            Member member
    ) {
        return MemberResDTO.MyProfile.builder()
                .coin(member.getCoin())
                .email(member.getEmail())
                .streak(member.getStreak())
                .nickname(member.getNickname())
                .build();
    }

    // 친구 요청 목록 조회
    public static MemberResDTO.FriendRequestList toFriendRequestList(
            Friend friend
    ) {
        return MemberResDTO.FriendRequestList.builder()
                .id(friend.getId())
                .fromMemberId(friend.getFromMember().getId())
                .nickname(friend.getFromMember().getNickname())
                .state(friend.getFriendState())
                .build();
    }

    // 친구 요청 상태 변경
    public static MemberResDTO.UpdateFriendRequest toUpdateFriendRequest(
            Member friend,
            FriendState state
    ) {
        return MemberResDTO.UpdateFriendRequest.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .state(state)
                .build();
    }

    // 사용자 단순 조회
    public static MemberResDTO.SearchMember toSearchMember(
            Member member
    ) {
        return MemberResDTO.SearchMember.builder()
                .email(member.getEmail())
                .id(member.getId())
                .nickname(member.getNickname())
                .build();
    }

    // 친구 목록 조회
    public static MemberResDTO.FriendList toFriendList(
            Friend friend
    ) {
        return MemberResDTO.FriendList.builder()
                .toMemberId(friend.getToMember().getId())
                .state(friend.getFriendState())
                .build();
    }

    // 친구 프로필 조회
    public static MemberResDTO.FriendProfile toFriendProfile(
            Member friend
    ) {
        return MemberResDTO.FriendProfile.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .coin(friend.getCoin())
                .streak(friend.getStreak())
                .loginAt(friend.getLoginAt())
                .build();
    }

    // 사용자 차단
    public static MemberResDTO.Blocking toBlocking(
            Member friend
    ) {
        return MemberResDTO.Blocking.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .blockedAt(LocalDateTime.now())
                .build();
    }
}
