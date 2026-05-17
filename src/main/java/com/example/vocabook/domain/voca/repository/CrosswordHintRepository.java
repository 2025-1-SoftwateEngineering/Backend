package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrosswordHintRepository extends JpaRepository<CrosswordHint, Long> {
    List<CrosswordHint> findAllByCrossword(Crossword crossword);

    Long countByCrossword(Crossword crossword);
}
