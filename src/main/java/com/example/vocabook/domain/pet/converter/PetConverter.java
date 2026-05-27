package com.example.vocabook.domain.pet.converter;

import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import com.example.vocabook.domain.pet.dto.PetResDTO;

public class PetConverter {

	public static PetResDTO.PetInfo toPetInfo(MemberPet pet, long foodCount, long waterCount, String petImageUrl) {
		return PetResDTO.PetInfo.builder()
				.petId(pet.getId())
				.level(pet.getCurrentLevel())
				.stage(pet.getStage())
				.currentXp(pet.getCurrentExp())
				.hunger(pet.getHunger())
				.thirst(pet.getThirst())
				.activeBackgroundItemId(pet.getActiveBackground() != null ? pet.getActiveBackground().getId() : null)
				.activeAccessoryItemId(pet.getActiveAccessory() != null ? pet.getActiveAccessory().getId() : null)
				.petImageUrl(petImageUrl)
				.activeBackgroundUrl(pet.getActiveBackground() != null ? pet.getActiveBackground().getImageUrl() : null)
				.activeAccessoryUrl(pet.getActiveAccessory() != null ? pet.getActiveAccessory().getImageUrl() : null)
				.foodCount(foodCount)
				.waterCount(waterCount)
				.build();
	}
}
