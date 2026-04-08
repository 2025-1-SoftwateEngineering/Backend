package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.voca.entity.Voca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberVocaRepository extends JpaRepository<MemberVoca, Long> {

	Optional<MemberVoca> findByMemberAndVoca(Member member, Voca voca);

	@Modifying
	@Query("UPDATE MemberVoca mv SET mv.correctCnt = :correctCnt, mv.learningWordCnt = :learningWordCnt, mv.solvedAt = :solvedAt WHERE mv.member = :member AND mv.voca = :voca")
	void updateResult(
			@Param("member") Member member,
			@Param("voca") Voca voca,
			@Param("correctCnt") Long correctCnt,
			@Param("learningWordCnt") Long learningWordCnt,
			@Param("solvedAt") LocalDateTime solvedAt
	);
}