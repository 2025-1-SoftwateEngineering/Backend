package com.example.vocabook.domain.voca.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class VocaException extends VocaBookException {
    public VocaException(BaseErrorCode code) {
        super(code);
    }
}
