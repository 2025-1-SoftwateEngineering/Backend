package com.example.vocabook.domain.store.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class StoreException extends VocaBookException {
    public StoreException(BaseErrorCode code) {
        super(code);
    }
}
