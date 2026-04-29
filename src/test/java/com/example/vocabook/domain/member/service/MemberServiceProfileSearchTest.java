package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.exception.MemberException;
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
@DisplayName("MemberService 프로필 및 검색 테스트")
class MemberServiceProfileSearchTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private AuthMember authMember;
    private Member myMember;

    @BeforeEach
    void setUp() {
        myMember = Member.builder()
                .id(1L)
                .email("me@example.com")
                .build();
        authMember = new AuthMember(myMember);
    }

    @Test
    @DisplayName("사용자 단순 조회 실패 - 이메일에 해당하는 사용자 없음 (NOT_FOUND)")
    void searchMember_Fail_NotFound() {
        // given
        String email = "unknown@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        MemberReqDTO.SearchMember dto = new MemberReqDTO.SearchMember(email);

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.searchMember(authMember, dto)
        );

        // then
        assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
    }
}
