package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.pet.exception.PetException;
import com.example.vocabook.domain.pet.exception.code.PetErrorCode;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class PetBgStrategy implements ItemUseStrategy {

	private final MemberPetRepository memberPetRepository;

	@Override
	public boolean supports(ItemType itemType) {
		return itemType == ItemType.PET_BG_1 || itemType == ItemType.PET_BG_2;
	}

	@Override
	public void apply(Member member, MemberItem memberItem) {
		MemberPet pet = memberPetRepository.findByMember(member)
				.orElseThrow(() -> new PetException(PetErrorCode.PET_NOT_FOUND));

		pet.changeBackground(memberItem.getItem().getItemType());
	}
}
