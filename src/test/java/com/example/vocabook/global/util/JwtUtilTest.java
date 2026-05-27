package com.example.vocabook.global.util;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        // 최소 32바이트 이상 시크릿 키 필요
        String secret = "thisisaverysecuresecretkeyforjwttokengeneration";
        Long accessExpiration = 3600000L; // 1시간
        Long refreshExpiration = 86400000L; // 1일

        jwtUtil = new JwtUtil(secret, accessExpiration, refreshExpiration);

        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("Tester")
                .authorize(Authorize.ROLE_USER)
                .build();
        authMember = new AuthMember(member);
    }

    @Test
    @DisplayName("AccessToken 생성 및 파싱 성공")
    void createAccessToken_Success() {
        // when
        String accessToken = jwtUtil.createAccessToken(authMember);

        // then
        assertThat(accessToken).isNotBlank();
        assertThat(jwtUtil.isValid(accessToken)).isTrue();
        assertThat(jwtUtil.isRefresh(accessToken)).isFalse();
        assertThat(jwtUtil.getEmail(accessToken)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("RefreshToken 생성 및 파싱 성공")
    void createRefreshToken_Success() {
        // when
        String refreshToken = jwtUtil.createRefreshToken(authMember);

        // then
        assertThat(refreshToken).isNotBlank();
        assertThat(jwtUtil.isValid(refreshToken)).isTrue();
        assertThat(jwtUtil.isRefresh(refreshToken)).isTrue();
        assertThat(jwtUtil.getEmail(refreshToken)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰 만료 시간 추출 성공")
    void getExpiration_Success() {
        // when
        String token = jwtUtil.createAccessToken(authMember);
        Date expiration = jwtUtil.getExpiration(token);

        // then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void isValid_Fail_InvalidToken() {
        // when
        String invalidToken = "invalid.token.here";

        // then
        assertThat(jwtUtil.isValid(invalidToken)).isFalse();
        assertThat(jwtUtil.getEmail(invalidToken)).isNull();
        assertThat(jwtUtil.isRefresh(invalidToken)).isFalse();
    }
}
