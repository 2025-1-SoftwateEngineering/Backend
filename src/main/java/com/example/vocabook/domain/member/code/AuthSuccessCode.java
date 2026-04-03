package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    SIGN_UP(HttpStatus.OK,
            "AUTH200_1",
            "성공적으로 회원가입을 마쳤습니다."),
    LOGIN(HttpStatus.OK,
            "AUTH200_2",
            "성공적으로 로그인했습니다."),
    REISSUE(HttpStatus.OK,
            "AUTH200_3",
            "성공적으로 토큰을 재발급했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
