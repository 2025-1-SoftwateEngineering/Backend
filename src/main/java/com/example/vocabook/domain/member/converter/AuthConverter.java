package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.res.AuthResDTO;

import java.util.Date;

public class AuthConverter {

    // 회원가입
    public static AuthResDTO.SignUp toSignUp(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){
        return AuthResDTO.SignUp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshExpiration(refreshExpiration)
                .build();
    }

    // 로그인
    public static AuthResDTO.Login toLogin(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){
        return AuthResDTO.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshExpiration(refreshExpiration)
                .build();
    }

    // 토큰 재발급
    public static AuthResDTO.Reissue toReissue(
            String accessToken,
            String refreshToken,
            Date refreshExpiration
    ){
        return AuthResDTO.Reissue.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshExpiration(refreshExpiration)
                .build();
    }
}
