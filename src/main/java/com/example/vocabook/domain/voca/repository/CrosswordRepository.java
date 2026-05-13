package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Crossword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrosswordRepository extends JpaRepository<Crossword, Long> {
}
