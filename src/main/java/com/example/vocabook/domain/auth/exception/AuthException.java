package com.example.vocabook.domain.auth.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class AuthException extends VocaBookException {
    public AuthException(BaseErrorCode code) {
        super(code);
    }
}
