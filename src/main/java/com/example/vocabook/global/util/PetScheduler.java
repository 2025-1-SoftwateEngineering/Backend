package com.example.vocabook.global.util;

import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PetScheduler {

	private final MemberPetRepository memberPetRepository;

	@Scheduled(fixedRate = 3600000)
	@Transactional
	public void updatePetStats() {
		memberPetRepository.bulkUpdateStats();
	}
}
