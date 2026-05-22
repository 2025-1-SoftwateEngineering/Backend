package com.example.vocabook.domain.store.exception.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {

	ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404_1", "아이템을 찾을 수 없습니다."),
	INSUFFICIENT_COINS(HttpStatus.BAD_REQUEST, "STORE400_1", "코인이 부족합니다."),
	ITEM_NOT_OWNED(HttpStatus.NOT_FOUND, "STORE404_2", "보유하지 않은 아이템입니다."),
	ITEM_ALREADY_OWNED(HttpStatus.BAD_REQUEST, "STORE400_2", "이미 보유 중인 아이템입니다."),
	CROSSWORD_HINT_CONTEXT_REQUIRED(HttpStatus.BAD_REQUEST, "STORE400_3", "힌트 아이템 사용 시 contextId가 필요합니다."),
	CROSSWORD_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "STORE400_4", "진행 중인 십자말풀이가 없습니다."),
	CHOICE_TIME_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "STORE400_5", "동일한 시간 보너스 아이템이 이미 활성화 중입니다."),
	HINT_ALREADY_USED(HttpStatus.BAD_REQUEST, "STORE400_6", "해당 단어에 같은 힌트를 이미 사용했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
