package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {

    VOCA_NOT_FOUND(HttpStatus.NOT_FOUND,
            "ADMIN404_1",
            "해당 단어장을 찾을 수 없습니다."),
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND,
            "ADMIN404_2",
            "해당 단어를 찾을 수 없습니다."),
    INVADE_START_REGEX(HttpStatus.BAD_REQUEST,
            "ADMIN400_1",
            "시작 위치 패턴이 올바르지 않습니다. (숫자+공백+숫자, e.g.1 2)")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
