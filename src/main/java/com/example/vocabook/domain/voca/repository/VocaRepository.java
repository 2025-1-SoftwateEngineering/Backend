package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Voca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocaRepository extends JpaRepository<Voca, Long> {
}