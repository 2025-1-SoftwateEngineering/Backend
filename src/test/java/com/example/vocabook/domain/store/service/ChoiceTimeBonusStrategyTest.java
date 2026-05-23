package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.domain.store.service.strategy.ChoiceTimeBonusStrategy;
import com.example.vocabook.global.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChoiceTimeBonusStrategy 단위 테스트")
public class ChoiceTimeBonusStrategyTest {

	@Mock
	private RedisUtil redisUtil;

	private ChoiceTimeBonusStrategy strategy;

	private Member member;

	@BeforeEach
	void setUp() {
		strategy = new ChoiceTimeBonusStrategy(redisUtil);
		member = Member.builder()
					   .id(1L)
					   .email("test@example.com")
					   .nickname("테스터")
					   .password("password")
					   .refreshToken("token")
					   .coin(500L)
					   .build();
	}

	private MemberItem buildMemberItem(ItemType itemType) {
		Item item = Item.builder()
					   .id(1L)
					   .name("시간 보너스 아이템")
					   .price(50L)
					   .itemType(itemType)
					   .build();
		return MemberItem.builder()
					   .id(10L)
					   .member(member)
					   .item(item)
					   .build();
	}

	@Test
	@DisplayName("supports - CHOICE_TIME_10, CHOICE_TIME_30만 true")
	void supports_OnlyChoiceTimeTypes() {
		assertTrue(strategy.supports(ItemType.CHOICE_TIME_10));
		assertTrue(strategy.supports(ItemType.CHOICE_TIME_30));
		assertFalse(strategy.supports(ItemType.STREAK_FREEZE));
		assertFalse(strategy.supports(ItemType.PET_FOOD));
	}

	@Test
	@DisplayName("CHOICE_TIME_10 사용 성공 - Redis에 보너스 10 저장")
	void apply_Time10_SavesBonus10() {
		MemberItem memberItem = buildMemberItem(ItemType.CHOICE_TIME_10);
		String expectedKey = "bonus:choiceTime:10:1";
		given(redisUtil.hasKey(expectedKey)).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, null);

		assertTrue(result.isEmpty());
		verify(redisUtil).save(eq(expectedKey), eq(10), eq(Duration.ofHours(1)));
	}

	@Test
	@DisplayName("CHOICE_TIME_30 사용 성공 - Redis에 보너스 30 저장")
	void apply_Time30_SavesBonus30() {
		MemberItem memberItem = buildMemberItem(ItemType.CHOICE_TIME_30);
		String expectedKey = "bonus:choiceTime:30:1";
		given(redisUtil.hasKey(expectedKey)).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, null);

		assertTrue(result.isEmpty());
		verify(redisUtil).save(eq(expectedKey), eq(30), eq(Duration.ofHours(1)));
	}

	@Test
	@DisplayName("CHOICE_TIME_10 중복 사용 차단 - 동일 아이템 이미 활성화 중")
	void apply_Time10_Duplicate_Throws() {
		MemberItem memberItem = buildMemberItem(ItemType.CHOICE_TIME_10);
		given(redisUtil.hasKey("bonus:choiceTime:10:1")).willReturn(true);

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, memberItem, null));
		assertEquals(StoreErrorCode.CHOICE_TIME_ALREADY_ACTIVE, ex.getCode());
	}

	@Test
	@DisplayName("CHOICE_TIME_10과 CHOICE_TIME_30 동시 사용 허용 - 서로 다른 Redis 키 사용")
	void apply_Time10AndTime30_AllowedSimultaneously() {
		MemberItem item30 = buildMemberItem(ItemType.CHOICE_TIME_30);
		// 10번 아이템은 이미 활성화, 30번 아이템은 활성화 안됨
		given(redisUtil.hasKey("bonus:choiceTime:30:1")).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, item30, null);

		assertTrue(result.isEmpty());
		verify(redisUtil).save(eq("bonus:choiceTime:30:1"), eq(30), eq(Duration.ofHours(1)));
	}
}
