package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.FriendRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 친구 요청 관련 예외 테스트")
class MemberServiceFriendRequestTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private MemberService memberService;

    private AuthMember authMember;
    private Member myMember;
    private Member friendMember;

    @BeforeEach
    void setUp() {
        myMember = Member.builder().id(1L).email("me@example.com").build();
        authMember = new AuthMember(myMember);
        friendMember = Member.builder().id(2L).email("friend@example.com").build();
    }

    @Test
    @DisplayName("친구 요청 목록 조회 실패 - 잘못된 커서 (INVADED_CURSOR)")
    void getFriendRequestList_Fail_InvadedCursor() {
        String invalidCursor = "invalid-cursor";
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.getFriendRequestList(invalidCursor, 10, authMember)
        );
        assertEquals(MemberErrorCode.INVADED_CURSOR, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 요청보낼 사용자 없음 (NOT_FOUND)")
    void sendFriendRequest_Fail_NotFound() {
        given(memberRepository.findById(2L)).willReturn(Optional.empty());
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.sendFriendRequest(authMember, 2L)
        );
        assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 자기 자신에게 요청 시도 (EXISTS_SELF_REQUEST)")
    void sendFriendRequest_Fail_SelfRequest() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(myMember));
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.sendFriendRequest(authMember, 1L)
        );
        assertEquals(MemberErrorCode.EXISTS_SELF_REQUEST, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 이미 친구이거나 요청 상태 (EXISTS_FRIEND_REQUEST)")
    void sendFriendRequest_Fail_ExistsFriendRequest() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        // 조건식 첫번째: 상대방이 나에게 보낸 요청이 존재한다고 모킹
        given(friendRepository.existsByFromMemberAndToMember(friendMember, myMember)).willReturn(true);
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.sendFriendRequest(authMember, 2L)
        );
        assertEquals(MemberErrorCode.EXISTS_FRIEND_REQUEST, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 보내기 실패 - 친구가 나를 차단함 (BLOCKING)")
    void sendFriendRequest_Fail_Blocking() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        given(friendRepository.existsByFromMemberAndToMember(friendMember, myMember)).willReturn(false);
        given(friendRepository.existsByFromMemberAndToMember(myMember, friendMember)).willReturn(false);
        given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(friendMember, myMember, FriendState.BLOCKED)).willReturn(true);
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.sendFriendRequest(authMember, 2L)
        );
        assertEquals(MemberErrorCode.BLOCKING, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 상태 변경 실패 - 요청 보낸 사용자 없음 (NOT_FOUND)")
    void updateFriendRequest_Fail_NotFound() {
        given(memberRepository.findById(2L)).willReturn(Optional.empty());
        MemberReqDTO.UpdateFriendRequest dto = new MemberReqDTO.UpdateFriendRequest("accept");
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.updateFriendRequest(authMember, 2L, dto)
        );
        assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 상태 변경 실패 - 친구 요청 데이터 없음 (NOT_FOUND_FRIEND_REQUEST)")
    void updateFriendRequest_Fail_NotFoundFriendRequest() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        given(friendRepository.findByFromMemberAndToMember(friendMember, myMember)).willReturn(Optional.empty());
        MemberReqDTO.UpdateFriendRequest dto = new MemberReqDTO.UpdateFriendRequest("accept");
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.updateFriendRequest(authMember, 2L, dto)
        );
        assertEquals(MemberErrorCode.NOT_FOUND_FRIEND_REQUEST, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 상태 변경 실패 - 대기(WAITING) 상태가 아님 (NOT_REQUEST)")
    void updateFriendRequest_Fail_NotRequest() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        Friend acceptedReq = Friend.builder().fromMember(friendMember).toMember(myMember).friendState(FriendState.ACCEPTED).build();
        given(friendRepository.findByFromMemberAndToMember(friendMember, myMember)).willReturn(Optional.of(acceptedReq));
        MemberReqDTO.UpdateFriendRequest dto = new MemberReqDTO.UpdateFriendRequest("accept");
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.updateFriendRequest(authMember, 2L, dto)
        );
        assertEquals(MemberErrorCode.NOT_REQUEST, exception.getCode());
    }

    @Test
    @DisplayName("친구 요청 상태 변경 실패 - 유효하지 않은 상태 값 입력 (INVADED_STATE)")
    void updateFriendRequest_Fail_InvadedState() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        Friend waitingReq = Friend.builder().fromMember(friendMember).toMember(myMember).friendState(FriendState.WAITING).build();
        given(friendRepository.findByFromMemberAndToMember(friendMember, myMember)).willReturn(Optional.of(waitingReq));
        MemberReqDTO.UpdateFriendRequest dto = new MemberReqDTO.UpdateFriendRequest("invalid");
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.updateFriendRequest(authMember, 2L, dto)
        );
        assertEquals(MemberErrorCode.INVADED_STATE, exception.getCode());
    }
}
