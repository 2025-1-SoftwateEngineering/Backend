package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.entity.mapping.MemberChoice;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Crossword;
import com.google.cloud.storage.Blob;

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
                .authorize(member.getAuthorize())
                .profileUrl(member.getProfileUrl())
                .activeProfilePhoto(member.getActiveProfilePhoto())
                .activeProfileBg(member.getActiveProfileBg())
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
            Member friend,
            Integer totalWordLearned
    ) {
        return MemberResDTO.FriendProfile.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .totalWordsLearned(totalWordLearned)
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

    // 사용자 신고
    public static MemberResDTO.ReportMember toReportMember(
            Report report
    ){
        return MemberResDTO.ReportMember.builder()
                .id(report.getTargetMember().getId())
                .email(report.getTargetMember().getEmail())
                .reportedAt(LocalDateTime.now())
                .build();
    }

    // 사용자 사지선다
    public static MemberChoice toMemberChoice(
            Member member,
            Choice choice
    ){
        return MemberChoice.builder()
                .choice(choice)
                .member(member)
                .build();
    }

    // 사용자 십자말풀이
    public static MemberCrossword toMemberCross(
            Member member,
            Crossword crossword
    ) {
        return MemberCrossword.builder()
                .member(member)
                .crossword(crossword)
                .build();
    }

    // 사진 업로드용 URI 생성
    public static MemberResDTO.CreateSignedUri toCreateSignedUri(
            String fileName,
            String url,
            PhotoType photoType
    ){
        return MemberResDTO.CreateSignedUri.builder()
                .fileName(fileName)
                .url(url)
                .photoType(photoType)
                .build();
    }

    // 사진 업로드 완료
    public static MemberResDTO.UploadImage toUploadImage(
            Blob blob,
            String publicUrl
    ) {
        return MemberResDTO.UploadImage.builder()
                .uploadAt(blob.getUpdateTimeOffsetDateTime().toLocalDateTime())
                .publicUrl(publicUrl)
                .build();
    }

    // 프로필 변경
    public static MemberResDTO.UpdateProfile toUpdateProfile(
            Member member
    ) {
        return MemberResDTO.UpdateProfile.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }

    // 친구 삭제
    public static MemberResDTO.DeleteFriend toDeleteFriend(
            Member friend
    ) {
        return MemberResDTO.DeleteFriend.builder()
                .id(friend.getId())
                .email(friend.getEmail())
                .nickname(friend.getNickname())
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
