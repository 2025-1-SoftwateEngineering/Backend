package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
	Page<Word> findByVoca(Voca voca, Pageable pageable);
	List<Word> findByVoca(Voca voca);
}