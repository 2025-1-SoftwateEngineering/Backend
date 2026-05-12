package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Voca;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocaRepository extends JpaRepository<Voca, Long> {

    List<Voca> findByDescriptionIn(List<String> descriptions);

    @Query("SELECT v FROM Voca v ORDER BY v.id DESC")
    Slice<Voca> findVocaListWithoutCursor(Pageable pageable);

    @Query("SELECT v FROM Voca v WHERE v.id < :idCursor ORDER BY v.id DESC")
    Slice<Voca> findVocaListWithCursor(@Param("idCursor") Long idCursor, Pageable pageable);
}