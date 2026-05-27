package com.example.vocabook.global.security.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "test@example.com";
        Member member = Member.builder()
                .id(1L)
                .email(email)
                .password("encoded_password")
                .build();
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("encoded_password");
        assertThat(userDetails).isInstanceOf(AuthMember.class);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 사용자 없음")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // given
        String email = "notfound@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(MemberException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.MemberErrorCode.NOT_FOUND);
    }
}
