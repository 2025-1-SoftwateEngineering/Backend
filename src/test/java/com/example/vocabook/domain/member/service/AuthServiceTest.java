package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.AuthResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.exception.AuthException;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.MemberActiveProfileRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MemberActiveProfileRepository memberActiveProfileRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        AuthReqDTO.SignUp request = new AuthReqDTO.SignUp("test@example.com", "password", "Tester");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(passwordEncoder.encode(request.password())).willReturn("encoded_password");
        
        given(jwtUtil.createAccessToken(any(AuthMember.class))).willReturn("accessToken");
        given(jwtUtil.createRefreshToken(any(AuthMember.class))).willReturn("refreshToken");
        given(jwtUtil.getExpiration("refreshToken")).willReturn(new Date());

        Item profileItem = Item.builder().id(1L).name("기본 프로필 사진").build();
        Item bgItem = Item.builder().id(2L).name("기본 프로필 배경").build();
        given(itemRepository.findByName("기본 프로필 사진")).willReturn(profileItem);
        given(itemRepository.findByName("기본 프로필 배경")).willReturn(bgItem);
        
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return member;
        });

        // when
        AuthResDTO.SignUp response = authService.signUp(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_DuplicateEmail_ThrowsException() {
        // given
        AuthReqDTO.SignUp request = new AuthReqDTO.SignUp("test@example.com", "password", "Tester");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(AuthException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.AuthErrorCode.ALREADY_EXISTS);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        AuthReqDTO.Login request = new AuthReqDTO.Login("test@example.com", "password");
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .isSuspended(false)
                .build();
        
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);
        
        given(jwtUtil.createAccessToken(any(AuthMember.class))).willReturn("accessToken");
        given(jwtUtil.createRefreshToken(any(AuthMember.class))).willReturn("refreshToken");
        given(jwtUtil.getExpiration("refreshToken")).willReturn(new Date());

        // when
        AuthResDTO.Login response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("로그인 실패 - 없는 이메일")
    void login_NotFoundEmail_ThrowsException() {
        // given
        AuthReqDTO.Login request = new AuthReqDTO.Login("test@example.com", "password");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 정지된 유저")
    void login_SuspendedUser_ThrowsException() {
        // given
        AuthReqDTO.Login request = new AuthReqDTO.Login("test@example.com", "password");
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .isSuspended(true)
                .build();
        
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.MemberErrorCode.SUSPENDED);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_WrongPassword_ThrowsException() {
        // given
        AuthReqDTO.Login request = new AuthReqDTO.Login("test@example.com", "wrong_password");
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .isSuspended(false)
                .build();
        
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.AuthErrorCode.WRONG_PASSWORD);
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() {
        // given
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("oldRefreshToken");
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .refreshToken("oldRefreshToken")
                .build();
        
        given(jwtUtil.isRefresh(request.refreshToken())).willReturn(true);
        given(jwtUtil.getEmail(request.refreshToken())).willReturn("test@example.com");
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        
        given(jwtUtil.createAccessToken(any(AuthMember.class))).willReturn("newAccessToken");
        given(jwtUtil.createRefreshToken(any(AuthMember.class))).willReturn("newRefreshToken");
        given(jwtUtil.getExpiration("newRefreshToken")).willReturn(new Date());

        // when
        AuthResDTO.Reissue response = authService.reissue(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("newAccessToken");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰이 아님")
    void reissue_NotRefreshToken_ThrowsException() {
        // given
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("notRefreshToken");
        given(jwtUtil.isRefresh(request.refreshToken())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
                .isInstanceOf(AuthException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.AuthErrorCode.NOT_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 저장된 토큰과 다름")
    void reissue_InvalidRefreshToken_ThrowsException() {
        // given
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("differentRefreshToken");
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .refreshToken("actualRefreshToken")
                .build();
        
        given(jwtUtil.isRefresh(request.refreshToken())).willReturn(true);
        given(jwtUtil.getEmail(request.refreshToken())).willReturn("test@example.com");
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
                .isInstanceOf(AuthException.class)
                .extracting("code")
                .isEqualTo(com.example.vocabook.domain.member.code.AuthErrorCode.INVAILD_REFRESH_TOKEN);
    }
}
