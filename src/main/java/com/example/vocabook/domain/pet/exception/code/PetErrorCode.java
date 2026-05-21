package com.example.vocabook.domain.pet.exception.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PetErrorCode implements BaseErrorCode {

	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET404_1", "펫을 찾을 수 없습니다."),
	PET_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PET400_1", "이미 펫이 존재합니다."),
	HUNGER_ALREADY_ZERO(HttpStatus.BAD_REQUEST, "PET400_2", "배고픔 수치가 0입니다."),
	THIRST_ALREADY_ZERO(HttpStatus.BAD_REQUEST, "PET400_3", "목마름 수치가 0입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
