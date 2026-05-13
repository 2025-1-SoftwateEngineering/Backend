package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
}
