package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
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
import org.mockito.InjectMocks;
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

	@InjectMocks
	private PetBgStrategy strategy;

	private Member member;
	private MemberPet pet;

	@BeforeEach
	void setUp() {
		member = Member.builder()
				.id(1L)
				.email("test@example.com")
				.build();

		pet = MemberPet.builder()
				.id(100L)
				.member(member)
				.build();
	}

	private MemberItem buildMemberItem(ItemType type, Long itemId) {
		Item item = Item.builder().id(itemId).name(type.name()).itemType(type).build();
		return MemberItem.builder().id(10L).member(member).item(item).count(1L).build();
	}

	@Test
	@DisplayName("지원하는 아이템 타입인지 확인 (PET_BG)")
	void supports_Success() {
		assertTrue(strategy.supports(ItemType.PET_BG));
		assertFalse(strategy.supports(ItemType.PROFILE_PHOTO));
	}

	@Test
	@DisplayName("펫 배경화면 변경 성공 - 펫이 존재하는 경우")
	void apply_Success() {
		MemberItem memberItem = buildMemberItem(ItemType.PET_BG, 1L);
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, null);

		assertTrue(result.isEmpty());
		assertEquals(1L, pet.getActiveBackground().getId());
	}

	@Test
	@DisplayName("펫 배경화면 변경 성공 - 기존 장착 아이템과 다를 때 교체")
	void apply_Success_ChangeBackground() {
		MemberItem oldItem = buildMemberItem(ItemType.PET_BG, 1L);
		MemberItem newItem = buildMemberItem(ItemType.PET_BG, 2L);

		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		strategy.apply(member, oldItem, null);
		strategy.apply(member, newItem, null);

		assertEquals(2L, pet.getActiveBackground().getId());
	}

	@Test
	@DisplayName("펫 배경화면 변경 실패 - 이미 동일한 배경화면을 장착 중인 경우")
	void apply_Fail_AlreadyEquipped() {
		MemberItem memberItem = buildMemberItem(ItemType.PET_BG, 1L);
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		strategy.apply(member, memberItem, null);

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, memberItem, null));

		assertEquals(StoreErrorCode.DECORATION_ALREADY_EQUIPPED, ex.getCode());
	}

	@Test
	@DisplayName("펫 배경화면 변경 실패 - 펫이 존재하지 않는 경우")
	void apply_Fail_PetNotFound() {
		MemberItem memberItem = buildMemberItem(ItemType.PET_BG, 1L);
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());

		com.example.vocabook.domain.pet.exception.PetException ex = assertThrows(com.example.vocabook.domain.pet.exception.PetException.class,
				() -> strategy.apply(member, memberItem, null));

		assertEquals(com.example.vocabook.domain.pet.exception.code.PetErrorCode.PET_NOT_FOUND, ex.getCode());
	}
}
