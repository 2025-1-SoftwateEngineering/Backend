package com.example.vocabook.domain.alert.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class AlertException extends VocaBookException {
    public AlertException(BaseErrorCode code) {
        super(code);
    }
}
