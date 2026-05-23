package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.service.strategy.ProfileDecorationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileDecorationStrategy 단위 테스트")
public class ProfileDecorationStrategyTest {

	private ProfileDecorationStrategy strategy;
	private Member member;

	@BeforeEach
	void setUp() {
		strategy = new ProfileDecorationStrategy();

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
				.name("프로필 아이템")
				.price(300L)
				.itemType(itemType)
				.build();
		return MemberItem.builder()
				.id(10L)
				.member(member)
				.item(item)
				.build();
	}

	@Test
	@DisplayName("supports - 프로필 4종류만 true")
	void supports_OnlyProfileTypes() {
		assertTrue(strategy.supports(ItemType.PROFILE_PHOTO_1));
		assertTrue(strategy.supports(ItemType.PROFILE_PHOTO_2));
		assertTrue(strategy.supports(ItemType.PROFILE_BG_1));
		assertTrue(strategy.supports(ItemType.PROFILE_BG_2));
		assertFalse(strategy.supports(ItemType.PET_BG_1));
		assertFalse(strategy.supports(ItemType.STREAK_FREEZE));
		assertFalse(strategy.supports(ItemType.PET_FOOD));
	}

	@Test
	@DisplayName("PROFILE_PHOTO_1 적용 - activeProfilePhoto가 PROFILE_PHOTO_1로 변경")
	void apply_ProfilePhoto1_SetsActiveProfilePhoto() {
		Optional<StoreResDTO.HintResult> result = strategy.apply(member, buildMemberItem(ItemType.PROFILE_PHOTO_1), null);

		assertTrue(result.isEmpty());
		assertEquals(ItemType.PROFILE_PHOTO_1, member.getActiveProfilePhoto());
		assertNull(member.getActiveProfileBg());
	}

	@Test
	@DisplayName("PROFILE_PHOTO_2 적용 - activeProfilePhoto가 PROFILE_PHOTO_2로 변경")
	void apply_ProfilePhoto2_SetsActiveProfilePhoto() {
		Optional<StoreResDTO.HintResult> result = strategy.apply(member, buildMemberItem(ItemType.PROFILE_PHOTO_2), null);

		assertTrue(result.isEmpty());
		assertEquals(ItemType.PROFILE_PHOTO_2, member.getActiveProfilePhoto());
		assertNull(member.getActiveProfileBg());
	}

	@Test
	@DisplayName("PROFILE_BG_1 적용 - activeProfileBg가 PROFILE_BG_1로 변경")
	void apply_ProfileBg1_SetsActiveProfileBg() {
		Optional<StoreResDTO.HintResult> result = strategy.apply(member, buildMemberItem(ItemType.PROFILE_BG_1), null);

		assertTrue(result.isEmpty());
		assertEquals(ItemType.PROFILE_BG_1, member.getActiveProfileBg());
		assertNull(member.getActiveProfilePhoto());
	}

	@Test
	@DisplayName("PROFILE_BG_2 적용 - activeProfileBg가 PROFILE_BG_2로 변경")
	void apply_ProfileBg2_SetsActiveProfileBg() {
		Optional<StoreResDTO.HintResult> result = strategy.apply(member, buildMemberItem(ItemType.PROFILE_BG_2), null);

		assertTrue(result.isEmpty());
		assertEquals(ItemType.PROFILE_BG_2, member.getActiveProfileBg());
		assertNull(member.getActiveProfilePhoto());
	}

	@Test
	@DisplayName("사진 연속 변경 - 나중에 사용한 사진으로 덮어씌워짐")
	void apply_PhotoOverride_UpdatesToLatest() {
		strategy.apply(member, buildMemberItem(ItemType.PROFILE_PHOTO_1), null);
		assertEquals(ItemType.PROFILE_PHOTO_1, member.getActiveProfilePhoto());

		strategy.apply(member, buildMemberItem(ItemType.PROFILE_PHOTO_2), null);
		assertEquals(ItemType.PROFILE_PHOTO_2, member.getActiveProfilePhoto());
	}

	@Test
	@DisplayName("사진과 배경은 서로 독립적으로 적용됨")
	void apply_PhotoAndBg_AreIndependent() {
		strategy.apply(member, buildMemberItem(ItemType.PROFILE_PHOTO_1), null);
		strategy.apply(member, buildMemberItem(ItemType.PROFILE_BG_2), null);

		assertEquals(ItemType.PROFILE_PHOTO_1, member.getActiveProfilePhoto());
		assertEquals(ItemType.PROFILE_BG_2, member.getActiveProfileBg());
	}
}
