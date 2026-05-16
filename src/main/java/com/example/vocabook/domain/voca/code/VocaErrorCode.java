package com.example.vocabook.domain.voca.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VocaErrorCode implements BaseErrorCode {

    NOT_PLAY_CHOICE(HttpStatus.BAD_REQUEST,
            "VOCA400_1",
            "사용자가 이 사지선다를 하지 않고있습니다."),
    ALREADY_CLEAR(HttpStatus.BAD_REQUEST,
            "VOCA400_2",
            "해당 사지선다를 이미 완료했습니다."),
    MISMATCH_MEMBER(HttpStatus.BAD_REQUEST,
            "VOCA400_3",
            "해당 사지선다를 진행한 사용자가 아닙니다."),
	VOCA_NOT_FOUND(HttpStatus.NOT_FOUND, "VOCA404_1", "단어장을 찾을 수 없습니다."),
	WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "VOCA404_2", "단어를 찾을 수 없습니다."),
    CHOICE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "VOCA404_3",
            "사지선다를 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}