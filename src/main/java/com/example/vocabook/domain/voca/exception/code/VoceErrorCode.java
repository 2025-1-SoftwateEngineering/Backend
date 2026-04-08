package com.example.vocabook.domain.voca.exception.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VoceErrorCode implements BaseErrorCode {

	VOCA_NOT_FOUND(HttpStatus.NOT_FOUND, "VOCA404_1", "단어장을 찾을 수 없습니다."),
	WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "VOCA404_2", "단어를 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}