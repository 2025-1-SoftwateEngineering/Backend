package com.example.vocabook.domain.alert.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AlertErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "ALERT404_1",
            "알림을 찾지 못했습니다."),
    NOT_FOUND_FCM(HttpStatus.NOT_FOUND,
            "ALERT404_2",
            "FCM 토큰을 찾지 못했습니다."),
    ALREADY_SET_ALERT(HttpStatus.CONFLICT,
            "ALERT409_1",
            "이미 설정된 알람이 존재합니다."),
    INVADED_ALERT_TYPE(HttpStatus.BAD_REQUEST,
            "ALERT400_1",
            "지원하지 않는 알림 타입입니다."),
    INVADED_ALERT_TIME(HttpStatus.BAD_REQUEST,
            "ALERT400_2",
            "이미 지난 시간입니다."),
    MISMATCH_USER(HttpStatus.BAD_REQUEST,
            "ALERT400_3",
            "알림 설정한 사용자가 아닙니다."),
    INVADED_CURSOR(HttpStatus.BAD_REQUEST,
            "ALERT400_4",
            "커서를 "),
    FAILED_SET_ALERT(HttpStatus.INTERNAL_SERVER_ERROR,
            "ALERT500_1",
            "예기치 못한 상황으로 알림을 설정하지 못했습니다."),
    FAILED_SEND_ALERT(HttpStatus.INTERNAL_SERVER_ERROR,
            "ALERT500_2",
            "FCM 서버의 예기치 못한 상황으로 알림을 전송하지 못했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
