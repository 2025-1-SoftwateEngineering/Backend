package com.example.vocabook.global.apiPayload.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VocaBookException extends RuntimeException {
    private final BaseErrorCode code;
}
