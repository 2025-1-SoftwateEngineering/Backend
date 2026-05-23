package com.example.vocabook.domain.pet.exception;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.exception.VocaBookException;

public class PetException extends VocaBookException {
	public PetException(BaseErrorCode code) {
		super(code);
	}
}
