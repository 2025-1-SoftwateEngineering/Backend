package com.example.vocabook.domain.store.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404_1", "아이템을 찾을 수 없습니다."),
    INSUFFICIENT_COINS(HttpStatus.BAD_REQUEST, "STORE400_1", "코인이 부족합니다."),
    ITEM_NOT_OWNED(HttpStatus.NOT_FOUND, "STORE404_2", "보유하지 않은 아이템입니다."),
    ITEM_ALREADY_OWNED(HttpStatus.BAD_REQUEST, "STORE400_2", "이미 보유 중인 아이템입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
