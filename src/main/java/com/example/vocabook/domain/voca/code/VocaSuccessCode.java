package com.example.vocabook.domain.voca.code;

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
			"채점 결과를 성공적으로 반환했습니다."),
	GET_STUDIED_VOCAS(HttpStatus.OK,
			"VOCA200_4",
			"학습한 단어장 목록을 성공적으로 불러왔습니다."),
	GET_VOCA_LIST(HttpStatus.OK,
			"VOCA200_5",
			"단어장 목록을 성공적으로 불러왔습니다."),
	MEMORIZE(HttpStatus.OK,
			"VOCA200_7",
			"암기한 단어를 성공적으로 저장했습니다."),
	GET_MEMORIZED_WORDS(HttpStatus.OK,
			"VOCA200_8",
			"암기한 단어 목록을 성공적으로 불러왔습니다."),
	COMPLETE_TEST(HttpStatus.OK,
			"VOCA200_9",
			"테스트를 성공적으로 완료했습니다."),
    GET_CHOICE_LIST(HttpStatus.OK,
            "VOCA200_4",
            "성공적으로 사지선다 문제 목록을 조회했습니다."),
    GET_CHOICE(HttpStatus.OK,
            "VOCA200_5",
            "성공적으로 사지선다 선택지를 조회했습니다."),
    SUBMIT_CHOICE(HttpStatus.OK,
            "VOCA200_6",
            "성공적으로 사지선다 정답을 제출했습니다."),
    GET_CHOICE_NO_CONTENT(HttpStatus.NO_CONTENT,
            "VOCA204_1",
            "마지막 문제였습니다. 결과를 조회해주세요."),

    ;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
