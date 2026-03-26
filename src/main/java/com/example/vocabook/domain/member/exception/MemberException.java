package com.example.vocabook.domain.member.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class MemberException extends VocaBookException {
    public MemberException(BaseErrorCode code) {
        super(code);
    }
}
