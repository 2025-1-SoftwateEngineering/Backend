package com.example.vocabook.domain.voca.exception.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VocaSuccessCode implements BaseSuccessCode {

	GET_WORDS(HttpStatus.OK,
			"VOCA200_1",
			"단어 목록을 성공적으로 불러왔습니다."),
	GET_TEST(HttpStatus.OK,
			"VOCA200_2",
			"테스트 문제를 성공적으로 불러왔습니다."),
	SUBMIT_TEST(HttpStatus.OK,
			"VOCA200_3",
			"테스트 결과를 성공적으로 제출했습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}