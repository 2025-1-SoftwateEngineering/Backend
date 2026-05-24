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
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.domain.store.service.strategy.PetBgStrategy;
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
@DisplayName("PetBgStrategy 단위 테스트")
public class PetBgStrategyTest {

	@Mock
	private MemberPetRepository memberPetRepository;

	private PetBgStrategy strategy;
	private Member member;

	@BeforeEach
	void setUp() {
		strategy = new PetBgStrategy(memberPetRepository);

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
				.name("펫 배경")
				.price(200L)
				.itemType(itemType)
				.build();
		return MemberItem.builder()
				.id(10L)
				.member(member)
				.item(item)
				.build();
	}

	@Test
	@DisplayName("supports - PET_BG_1, PET_BG_2만 true")
	void supports_OnlyPetBg() {
		assertTrue(strategy.supports(ItemType.PET_BG_1));
		assertTrue(strategy.supports(ItemType.PET_BG_2));
		assertFalse(strategy.supports(ItemType.PET_FOOD));
		assertFalse(strategy.supports(ItemType.PROFILE_PHOTO_1));
	}

	@Test
	@DisplayName("PET_BG_1 적용 성공 - activeBackground 변경")
	void apply_PetBg1_Success() {
		MemberPet pet = MemberPet.builder().id(1L).member(member).build();
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, buildMemberItem(ItemType.PET_BG_1), null);

		assertTrue(result.isEmpty());
		assertEquals(ItemType.PET_BG_1, pet.getActiveBackground());
	}

	@Test
	@DisplayName("PET_BG_1 장착 중 PET_BG_2 사용 - 자동 교체")
	void apply_Bg1_ThenBg2_AutoReplaces() {
		MemberPet pet = MemberPet.builder().id(1L).member(member).build();
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		strategy.apply(member, buildMemberItem(ItemType.PET_BG_1), null);
		strategy.apply(member, buildMemberItem(ItemType.PET_BG_2), null);

		assertEquals(ItemType.PET_BG_2, pet.getActiveBackground());
	}

	@Test
	@DisplayName("이미 장착 중인 배경 재사용 시 예외")
	void apply_SameBg_AlreadyEquipped_Throws() {
		MemberPet pet = MemberPet.builder().id(1L).member(member).build();
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		strategy.apply(member, buildMemberItem(ItemType.PET_BG_1), null);

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, buildMemberItem(ItemType.PET_BG_1), null));
		assertEquals(StoreErrorCode.DECORATION_ALREADY_EQUIPPED, ex.getCode());
	}

	@Test
	@DisplayName("펫 없음 - PET_NOT_FOUND 예외")
	void apply_PetNotFound_Throws() {
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());

		PetException ex = assertThrows(PetException.class,
				() -> strategy.apply(member, buildMemberItem(ItemType.PET_BG_1), null));
		assertEquals(PetErrorCode.PET_NOT_FOUND, ex.getCode());
	}
}
