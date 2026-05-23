package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.pet.exception.PetException;
import com.example.vocabook.domain.pet.exception.code.PetErrorCode;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.service.strategy.PetWaterStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetWaterStrategy 단위 테스트")
public class PetWaterStrategyTest {

	@Mock
	private MemberPetRepository memberPetRepository;

	private PetWaterStrategy strategy;

	private Member member;
	private MemberItem memberItem;

	@BeforeEach
	void setUp() {
		strategy = new PetWaterStrategy(memberPetRepository);

		member = Member.builder()
				.id(1L)
				.email("test@example.com")
				.nickname("테스터")
				.password("password")
				.refreshToken("token")
				.coin(500L)
				.build();

		Item item = Item.builder()
				.id(2L)
				.name("물")
				.price(65L)
				.itemType(ItemType.PET_WATER)
				.build();

		memberItem = MemberItem.builder()
				.id(10L)
				.member(member)
				.item(item)
				.build();
	}

	@Test
	@DisplayName("supports - PET_WATER만 true")
	void supports_OnlyPetWater() {
		assertTrue(strategy.supports(ItemType.PET_WATER));
		assertFalse(strategy.supports(ItemType.PET_FOOD));
		assertFalse(strategy.supports(ItemType.STREAK_FREEZE));
	}

	@Test
	@DisplayName("물 사용 성공 - 목마름 감소 및 경험치 획득")
	void apply_Success_DecreasesThirstAndGainsXp() {
		MemberPet pet = MemberPet.builder()
				.id(1L).member(member).thirst(50).build();

		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, null);

		assertTrue(result.isEmpty());
		assertEquals(26, pet.getThirst()); // 50 - 24
		assertTrue(pet.getCurrentExp() > 0);
	}

	@Test
	@DisplayName("물 사용 성공 - 목마름이 24 미만이면 전부 차감")
	void apply_Success_ThirstLessThan24() {
		MemberPet pet = MemberPet.builder()
				.id(1L).member(member).thirst(10).build();

		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		strategy.apply(member, memberItem, null);

		assertEquals(0, pet.getThirst());
		assertEquals(10, pet.getCurrentExp()); // 실제 차감량만큼 xp
	}

	@Test
	@DisplayName("물 사용 실패 - 목마름이 이미 0")
	void apply_ThirstAlreadyZero_Throws() {
		MemberPet pet = MemberPet.builder()
				.id(1L).member(member).thirst(0).build();

		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		PetException ex = assertThrows(PetException.class,
				() -> strategy.apply(member, memberItem, null));
		assertEquals(PetErrorCode.THIRST_ALREADY_ZERO, ex.getCode());
	}

	@Test
	@DisplayName("물 사용 실패 - 펫 없음")
	void apply_PetNotFound_Throws() {
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());

		PetException ex = assertThrows(PetException.class,
				() -> strategy.apply(member, memberItem, null));
		assertEquals(PetErrorCode.PET_NOT_FOUND, ex.getCode());
	}
}
