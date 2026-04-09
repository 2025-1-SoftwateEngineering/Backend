package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.voca.entity.Voca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberVocaRepository extends JpaRepository<MemberVoca, Long> {

	Optional<MemberVoca> findByMemberAndVoca(Member member, Voca voca);
}