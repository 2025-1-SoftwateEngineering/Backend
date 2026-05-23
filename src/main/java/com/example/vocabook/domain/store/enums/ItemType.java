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
	PET_BG_1(false),             // 펫 배경 1
	PET_BG_2(false),             // 펫 배경 2
	PROFILE_PHOTO_1(false),      // 프로필 사진 1
	PROFILE_PHOTO_2(false),      // 프로필 사진 2
	PROFILE_BG_1(false),         // 프로필 배경 1
	PROFILE_BG_2(false);         // 프로필 배경 2

	private final boolean allowDuplicate;
}
