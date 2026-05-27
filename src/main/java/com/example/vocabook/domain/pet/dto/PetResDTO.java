package com.example.vocabook.domain.pet.dto;

import com.example.vocabook.domain.pet.enums.PetStage;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.Builder;

public class PetResDTO {

	@Builder
	public record PetInfo(
			Long petId,
			int level,
			PetStage stage,
			long currentXp,
			int hunger,
			int thirst,
			Long activeBackgroundItemId,
			Long activeAccessoryItemId,
			String petImageUrl,
			String activeBackgroundUrl,
			String activeAccessoryUrl,
			long foodCount,
			long waterCount
	) {}
}
