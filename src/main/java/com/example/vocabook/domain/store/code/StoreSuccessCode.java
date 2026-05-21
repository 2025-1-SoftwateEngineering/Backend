package com.example.vocabook.domain.store.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreSuccessCode implements BaseSuccessCode {

	GET_ITEM_LIST(HttpStatus.OK, "STORE200_1", "상점 아이템 목록을 성공적으로 불러왔습니다."),
	PURCHASE_ITEM(HttpStatus.OK, "STORE200_2", "아이템을 성공적으로 구매했습니다."),
	GET_MY_ITEMS(HttpStatus.OK, "STORE200_3", "보유 아이템 목록을 성공적으로 불러왔습니다."),
	USE_ITEM(HttpStatus.OK, "STORE200_4", "아이템을 성공적으로 사용했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
