package com.example.vocabook.domain.pet.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.pet.dto.PetResDTO;
import com.example.vocabook.domain.pet.exception.PetException;
import com.example.vocabook.domain.pet.exception.code.PetErrorCode;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService 단위 테스트")
public class PetServiceTest {

	@Mock
	private MemberPetRepository memberPetRepository;
	@Mock
	private MemberItemRepository memberItemRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private com.example.vocabook.domain.member.repository.PetImageRepository petImageRepository;
	@Mock
	private com.example.vocabook.domain.store.repository.ItemRepository itemRepository;

	private PetService petService;

	private Member member;
	private AuthMember authMember;
	private MemberPet memberPet;

	@BeforeEach
	void setUp() {
		petService = new PetService(memberPetRepository, memberItemRepository, memberRepository, petImageRepository, itemRepository);

		member = Member.builder()
				.id(1L)
				.email("test@example.com")
				.nickname("테스터")
				.password("password")
				.refreshToken("token")
				.coin(200L)
				.build();

		authMember = new AuthMember(member);

		memberPet = MemberPet.builder()
				.id(1L)
				.member(member)
				.build();
	}

	@Test
	@DisplayName("펫 조회 성공")
	void getPet_Success() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(memberPet));
		given(memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_FOOD)).willReturn(2L);
		given(memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_WATER)).willReturn(1L);
		given(petImageRepository.findByPetStage(memberPet.getStage())).willReturn(Optional.of(com.example.vocabook.domain.member.entity.PetImage.builder().petStage(memberPet.getStage()).imageUrl("petUrl").build()));

		PetResDTO.PetInfo result = petService.getPet(authMember);

		assertNotNull(result);
		assertEquals(1L, result.petId());
		assertEquals(1, result.level());
		assertEquals(2L, result.foodCount());
		assertEquals(1L, result.waterCount());
		assertEquals(50, result.hunger());
		assertEquals(0, result.thirst());
		assertEquals("petUrl", result.petImageUrl());
	}

	@Test
	@DisplayName("펫 조회 실패 - 펫이 없는 경우")
	void getPet_PetNotFound() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());

		PetException ex = assertThrows(PetException.class, () -> petService.getPet(authMember));

		assertEquals(PetErrorCode.PET_NOT_FOUND, ex.getCode());
	}

	@Test
	@DisplayName("펫 생성 성공")
	void createPet_Success() {
		com.example.vocabook.domain.store.entity.Item bg = com.example.vocabook.domain.store.entity.Item.builder().name("bg").imageUrl("bgUrl").build();
		com.example.vocabook.domain.store.entity.Item acc = com.example.vocabook.domain.store.entity.Item.builder().name("acc").imageUrl("accUrl").build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());
		given(memberPetRepository.save(any(MemberPet.class))).willReturn(memberPet);
		given(itemRepository.findByName(com.example.vocabook.domain.member.service.PetImageInitializer.DEFAULT_BG_NAME)).willReturn(bg);
		given(itemRepository.findByName(com.example.vocabook.domain.member.service.PetImageInitializer.DEFAULT_ACCESSORY_NAME)).willReturn(acc);
		given(memberItemRepository.findByMemberAndItem(member, bg)).willReturn(Optional.empty());
		given(memberItemRepository.findByMemberAndItem(member, acc)).willReturn(Optional.empty());
		given(petImageRepository.findByPetStage(memberPet.getStage())).willReturn(Optional.of(com.example.vocabook.domain.member.entity.PetImage.builder().petStage(memberPet.getStage()).imageUrl("petUrl").build()));

		PetResDTO.PetInfo result = petService.createPet(authMember);

		assertNotNull(result);
		assertEquals(0L, result.foodCount());
		assertEquals(0L, result.waterCount());
		assertEquals("petUrl", result.petImageUrl());
	}

	@Test
	@DisplayName("펫 생성 실패 - 이미 펫이 있는 경우")
	void createPet_AlreadyExists() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(memberPet));

		PetException ex = assertThrows(PetException.class, () -> petService.createPet(authMember));

		assertEquals(PetErrorCode.PET_ALREADY_EXISTS, ex.getCode());
	}
}
