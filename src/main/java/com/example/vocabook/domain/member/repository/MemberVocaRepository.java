package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.voca.entity.Voca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberVocaRepository extends JpaRepository<MemberVoca, Long> {

	void deleteAllByVoca(Voca voca);

	Optional<MemberVoca> findByMemberAndVoca(Member member, Voca voca);

	// 멤버가 학습한 단어장 기록 전체 조회 (N+1 방지)
	@Query("SELECT mv FROM MemberVoca mv JOIN FETCH mv.voca WHERE mv.member = :member")
	List<MemberVoca> findAllByMember(Member member);
}