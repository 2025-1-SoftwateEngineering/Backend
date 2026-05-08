package com.example.vocabook.domain.alert.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AlertSuccessCode implements BaseSuccessCode {

    REGISTER_FCM(HttpStatus.OK,
            "ALERT200_1",
            "성공적으로 FCM 토큰을 등록했습니다."),
    CUSTOM_ALERT(HttpStatus.OK,
            "ALERT200_2",
            "성공적으로 알림을 등록했습니다."),
    GET_ALERT_LIST(HttpStatus.OK,
            "ALERT200_3",
            "성공적으로 알림 리스트를 조회했습니다."),
    DELETE_ALERT(HttpStatus.OK,
            "ALERT200_3",
            "성공적으로 알림을 삭제했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
