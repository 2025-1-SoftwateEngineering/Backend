package com.example.vocabook.domain.store.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemType {
	STREAK_FREEZE(true),         // 연속학습 파괴 방어권
	PET_FOOD(true),              // 사료
	PET_WATER(true),             // 물
	CHOICE_TIME_10(true),        // 사지선다 시간 +10초
	CHOICE_TIME_30(true),        // 사지선다 시간 +30초
	CROSSWORD_HINT_START(true),  // 십자말풀이 시작 스펠링 힌트
	CROSSWORD_HINT_MIDDLE(true), // 십자말풀이 중간 스펠링 힌트
	PET_BG(false),               // 펫 배경
	PET_ACCESSORY(false),        // 펫 악세서리
	PROFILE_PHOTO(false),        // 프로필 사진
	PROFILE_BG(false),           // 프로필 배경
	;

	private final boolean allowDuplicate;
}
