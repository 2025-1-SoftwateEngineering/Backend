package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.enums.ReportReason;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.*;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceActionTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MemberItemRepository memberItemRepository;

    @Mock
    private MemberActiveProfileRepository memberActiveProfileRepository;

    @InjectMocks
    private MemberService memberService;

    private Member myMember;
    private Member targetMember;
    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        myMember = Member.builder()
                .id(1L)
                .email("me@example.com")
                .password("encoded_password")
                .build();
        authMember = new AuthMember(myMember);
        targetMember = Member.builder()
                .id(2L)
                .email("target@example.com")
                .build();
    }

    @Test
    @DisplayName("사용자 차단 성공")
    void blockMember_Success() {
        // given
        given(memberRepository.findById(2L)).willReturn(Optional.of(targetMember));
        Friend myToFriend = Friend.builder().fromMember(myMember).toMember(targetMember).friendState(FriendState.ACCEPTED).build();
        given(friendRepository.findByFromMemberAndToMember(myMember, targetMember)).willReturn(Optional.of(myToFriend));
        given(friendRepository.findByFromMemberAndToMember(targetMember, myMember)).willReturn(Optional.empty());
        
        given(friendRepository.save(any(Friend.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        MemberResDTO.Blocking response = memberService.blockMember(authMember, 2L);

        // then
        assertThat(response).isNotNull();
        assertThat(myToFriend.getFriendState()).isEqualTo(FriendState.BLOCKED);
    }

    @Test
    @DisplayName("사용자 신고 성공")
    void reportMember_Success() {
        // given
        MemberReqDTO.ReportMember request = new MemberReqDTO.ReportMember(ReportReason.OTHER, "스팸입니다");
        given(memberRepository.findById(2L)).willReturn(Optional.of(targetMember));
        given(reportRepository.findAllByReportReasonAndTargetMember(ReportReason.OTHER, targetMember)).willReturn(Collections.emptyList());
        
        Report savedReport = Report.builder().id(100L).reportMember(myMember).targetMember(targetMember).reportReason(ReportReason.OTHER).build();
        given(reportRepository.save(any(Report.class))).willReturn(savedReport);

        // when
        MemberResDTO.ReportMember response = memberService.reportMember(authMember, 2L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 신고 실패 - 자기 자신 신고")
    void reportMember_SelfReport_ThrowsException() {
        // given
        MemberReqDTO.ReportMember request = new MemberReqDTO.ReportMember(ReportReason.OTHER, "스팸입니다");

        // when & then
        assertThatThrownBy(() -> memberService.reportMember(authMember, 1L, request))
                .isInstanceOf(MemberException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.MemberErrorCode.SELF_REPORT);
    }

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임 변경")
    void updateProfile_Success() {
        // given
        MemberReqDTO.UpdateProfile request = new MemberReqDTO.UpdateProfile("NewNickname", "", Collections.emptyList(), "password");
        given(memberRepository.findById(1L)).willReturn(Optional.of(myMember));
        given(passwordEncoder.matches(request.confirmPassword(), myMember.getPassword())).willReturn(true);

        // when
        MemberResDTO.UpdateProfile response = memberService.updateProfile(authMember, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("NewNickname");
    }

    @Test
    @DisplayName("친구 삭제 성공")
    void deleteFriend_Success() {
        // given
        given(memberRepository.findById(2L)).willReturn(Optional.of(targetMember));
        Friend myToFriend = Friend.builder().fromMember(myMember).toMember(targetMember).friendState(FriendState.ACCEPTED).build();
        given(friendRepository.findByFromMemberAndToMember(myMember, targetMember)).willReturn(Optional.of(myToFriend));
        given(friendRepository.findByFromMemberAndToMember(targetMember, myMember)).willReturn(Optional.empty());

        // when
        MemberResDTO.DeleteFriend response = memberService.deleteFriend(authMember, 2L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
    }
}
