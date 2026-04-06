package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    WRONG_PASSWORD(HttpStatus.BAD_REQUEST,
            "AUTH400_2",
            "아이디 혹은 비밀번호가 잘못되었습니다."),
    NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,
            "AUTH400_3",
            "리프레시 토큰이 아닙니다."),
    INVAILD_REFRESH_TOKEN(HttpStatus.BAD_REQUEST,
            "AUTH400_4",
            "리프레시 트큰이 맞지 않습니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT,
            "AUTH409_1",
            "이미 회원가입된 사용자입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
