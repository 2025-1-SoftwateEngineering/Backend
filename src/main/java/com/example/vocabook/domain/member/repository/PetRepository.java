package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
