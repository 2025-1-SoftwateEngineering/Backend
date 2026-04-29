package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import java.util.List;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 친구 관리 관련 예외 테스트")
class MemberServiceFriendManagementTest {

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
    @DisplayName("친구 목록 조회 실패 - 잘못된 커서 (INVADED_CURSOR)")
    void getFriendList_Fail_InvadedCursor() {
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.getFriendList(authMember, "invalid", 10)
        );
        assertEquals(MemberErrorCode.INVADED_CURSOR, exception.getCode());
    }

    @Test
    @DisplayName("친구 목록 초기 조회 성공 (cursor = -1)")
    void getFriendList_Success_FirstPage() {
        Friend friendRel = Friend.builder().id(100L).fromMember(myMember).toMember(friendMember).friendState(FriendState.ACCEPTED).build();
        Slice<Friend> slice = new SliceImpl<>(List.of(friendRel));
        given(friendRepository.findFriendListWithoutCursor(eq(myMember.getId()), eq("ACCEPTED"), any(PageRequest.class))).willReturn(slice);

        PagingResDTO.Cursor<MemberResDTO.FriendList> response = memberService.getFriendList(authMember, "-1", 10);

        assertNotNull(response);
        assertEquals("100", response.nextCursor());
        assertEquals(1, response.data().size());
    }

    @Test
    @DisplayName("친구 목록 이후 조회 성공 (cursor != -1)")
    void getFriendList_Success_NextPage() {
        Friend friendRel = Friend.builder().id(99L).fromMember(myMember).toMember(friendMember).friendState(FriendState.ACCEPTED).build();
        Slice<Friend> slice = new SliceImpl<>(List.of(friendRel));
        given(friendRepository.findFriendListWithCursor(eq(myMember.getId()), eq("ACCEPTED"), eq(100L), any(PageRequest.class))).willReturn(slice);

        PagingResDTO.Cursor<MemberResDTO.FriendList> response = memberService.getFriendList(authMember, "100", 10);

        assertNotNull(response);
        assertEquals("99", response.nextCursor());
        assertEquals(1, response.data().size());
    }

    @Test
    @DisplayName("친구 프로필 조회 실패 - 사용자 없음 (NOT_FOUND)")
    void getFriendProfile_Fail_NotFound() {
        given(memberRepository.findById(2L)).willReturn(Optional.empty());
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.getFriendProfile(authMember, 2L)
        );
        assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("친구 프로필 조회 실패 - 친구 아님 (NOT_FRIEND)")
    void getFriendProfile_Fail_NotFriend() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(myMember, friendMember, FriendState.ACCEPTED)).willReturn(false);
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.getFriendProfile(authMember, 2L)
        );
        assertEquals(MemberErrorCode.NOT_FRIEND, exception.getCode());
    }

    @Test
    @DisplayName("친구 프로필 조회 실패 - 상대방이 차단함 (BLOCKING)")
    void getFriendProfile_Fail_Blocking() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(friendMember));
        given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(myMember, friendMember, FriendState.ACCEPTED)).willReturn(true);
        given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(friendMember, myMember, FriendState.BLOCKED)).willReturn(true);
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.getFriendProfile(authMember, 2L)
        );
        assertEquals(MemberErrorCode.BLOCKING, exception.getCode());
    }

    @Test
    @DisplayName("사용자 차단 실패 - 차단할 사용자 없음 (NOT_FOUND)")
    void blockMember_Fail_NotFound() {
        given(memberRepository.findById(2L)).willReturn(Optional.empty());
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.blockMember(authMember, 2L)
        );
        assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
    }


}
