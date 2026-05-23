package com.example.vocabook.domain.pet.enums;

public enum PetStage {
	EGG, BABY, GROWING, ADULT;

	public static PetStage computeStage(int level) {
		if (level <= 2) return EGG;
		if (level <= 9) return BABY;
		if (level <= 24) return GROWING;
		return ADULT;
	}
}
