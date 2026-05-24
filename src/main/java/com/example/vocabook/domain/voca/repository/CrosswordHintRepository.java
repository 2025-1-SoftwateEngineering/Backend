package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrosswordHintRepository extends JpaRepository<CrosswordHint, Long> {
    List<CrosswordHint> findAllByCrossword(Crossword crossword);

    Long countByCrossword(Crossword crossword);

    @Query("SELECT ch FROM CrosswordHint ch JOIN FETCH ch.crossword WHERE ch.id = :id")
    Optional<CrosswordHint> findByIdWithCrossword(@Param("id") Long id);
}
