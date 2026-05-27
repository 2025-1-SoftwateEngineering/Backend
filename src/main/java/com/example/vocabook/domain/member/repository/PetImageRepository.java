package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.PetImage;
import com.example.vocabook.domain.pet.enums.PetStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PetImageRepository extends JpaRepository<PetImage, Long> {
    Optional<PetImage> findByPetStage(PetStage petStage);
}
