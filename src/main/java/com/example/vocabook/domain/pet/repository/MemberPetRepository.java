package com.example.vocabook.domain.pet.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberPetRepository extends JpaRepository<MemberPet, Long> {

	Optional<MemberPet> findByMember(Member member);

	@Modifying
	@Query(value = "UPDATE member_pet SET hunger = LEAST(hunger + 2, 250), thirst = LEAST(thirst + 1, 100) WHERE hunger < 250 OR thirst < 100", nativeQuery = true)
	void bulkUpdateStats();
}
