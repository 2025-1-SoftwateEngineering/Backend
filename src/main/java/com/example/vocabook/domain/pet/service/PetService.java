package com.example.vocabook.domain.pet.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.pet.converter.PetConverter;
import com.example.vocabook.domain.pet.dto.PetResDTO;
import com.example.vocabook.domain.pet.exception.PetException;
import com.example.vocabook.domain.pet.exception.code.PetErrorCode;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

	private final MemberPetRepository memberPetRepository;
	private final MemberItemRepository memberItemRepository;
	private final MemberRepository memberRepository;
	private final com.example.vocabook.domain.member.repository.PetImageRepository petImageRepository;
	private final com.example.vocabook.domain.store.repository.ItemRepository itemRepository;

	public PetResDTO.PetInfo getPet(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId()).orElseThrow();
		MemberPet pet = memberPetRepository.findByMember(member)
				.orElseThrow(() -> new PetException(PetErrorCode.PET_NOT_FOUND));
		long foodCount = memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_FOOD);
		long waterCount = memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_WATER);
		String petImageUrl = petImageRepository.findByPetStage(pet.getStage())
				.map(com.example.vocabook.domain.member.entity.PetImage::getImageUrl)
				.orElse("");
		return PetConverter.toPetInfo(pet, foodCount, waterCount, petImageUrl);
	}

	@Transactional
	public PetResDTO.PetInfo createPet(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId()).orElseThrow();
		if (memberPetRepository.findByMember(member).isPresent()) {
			throw new PetException(PetErrorCode.PET_ALREADY_EXISTS);
		}
		
		com.example.vocabook.domain.store.entity.Item defaultBg = itemRepository.findByName(com.example.vocabook.domain.member.service.PetImageInitializer.DEFAULT_BG_NAME);
		com.example.vocabook.domain.store.entity.Item defaultAcc = itemRepository.findByName(com.example.vocabook.domain.member.service.PetImageInitializer.DEFAULT_ACCESSORY_NAME);

		if (defaultBg != null && memberItemRepository.findByMemberAndItem(member, defaultBg).isEmpty()) {
			memberItemRepository.save(com.example.vocabook.domain.member.entity.mapping.MemberItem.builder()
					.member(member)
					.item(defaultBg)
					.build());
		}
		if (defaultAcc != null && memberItemRepository.findByMemberAndItem(member, defaultAcc).isEmpty()) {
			memberItemRepository.save(com.example.vocabook.domain.member.entity.mapping.MemberItem.builder()
					.member(member)
					.item(defaultAcc)
					.build());
		}

		MemberPet pet = MemberPet.builder()
				.member(member)
				.build();
		if (defaultBg != null) pet.changeBackground(defaultBg);
		if (defaultAcc != null) pet.changeAccessory(defaultAcc);
		
		MemberPet savedPet = memberPetRepository.save(pet);
		long foodCount = memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_FOOD);
		long waterCount = memberItemRepository.countByMemberAndItem_ItemType(member, ItemType.PET_WATER);
		String petImageUrl = petImageRepository.findByPetStage(savedPet.getStage())
				.map(com.example.vocabook.domain.member.entity.PetImage::getImageUrl)
				.orElse("");
		return PetConverter.toPetInfo(savedPet, foodCount, waterCount, petImageUrl);
	}
}
