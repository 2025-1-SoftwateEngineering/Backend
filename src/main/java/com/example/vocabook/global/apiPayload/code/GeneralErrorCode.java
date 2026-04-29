package com.example.vocabook.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode{

    BAD_REQUEST(HttpStatus.BAD_REQUEST,
            "COMMON400_1",
            "요청을 처리하지 못했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "COMMON401_1",
            "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,
            "COMMON403_1",
            "접근이 제한되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,
            "COMMON404_1",
            "해당 데이터를 찾지 못했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON500_1",
            "서버의 예기치 못한 오류입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST,
            "COMMON400_2",
            "타입을 잘못 입력했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
