package com.example.vocabook.domain.member.dto.res;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Date;

public class AuthResDTO {

    // 회원가입
    @Builder
    public record SignUp(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){}

    // 로그인
    @Builder
    public record Login(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){}

    // 토큰 재발급
    @Builder
    public record Reissue(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){}
}
