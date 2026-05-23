package com.example.vocabook.domain.pet.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PetSuccessCode implements BaseSuccessCode {

	GET_PET(HttpStatus.OK, "PET200_1", "펫 정보를 성공적으로 조회했습니다."),
	CREATE_PET(HttpStatus.OK, "PET200_2", "펫을 성공적으로 생성했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
