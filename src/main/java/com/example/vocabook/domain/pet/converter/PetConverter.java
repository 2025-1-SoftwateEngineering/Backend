package com.example.vocabook.domain.pet.converter;

import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.pet.dto.PetResDTO;

public class PetConverter {

	public static PetResDTO.PetInfo toPetInfo(MemberPet pet, long foodCount, long waterCount) {
		return PetResDTO.PetInfo.builder()
				.petId(pet.getId())
				.level(pet.getCurrentLevel())
				.stage(pet.getStage())
				.currentXp(pet.getCurrentExp())
				.hunger(pet.getHunger())
				.thirst(pet.getThirst())
				.activeBackground(pet.getActiveBackground())
				.foodCount(foodCount)
				.waterCount(waterCount)
				.build();
	}
}
