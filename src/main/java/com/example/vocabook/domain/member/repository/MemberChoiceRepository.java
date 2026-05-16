package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberChoice;
import com.example.vocabook.domain.voca.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberChoiceRepository extends JpaRepository<MemberChoice, Long> {

    Long member(Member member);

    @Query(
            value = "select mc " +
                    "from MemberChoice mc " +
                    "where mc.member = :member and mc.choice = :choice and mc.solvedAt is null " +
                    "order by mc.id desc " +
                    "limit 1 "
    )
    Optional<MemberChoice> findByMemberAndChoiceAndSolvedAtIsNull(Member member, Choice choice);

    Long countByMemberAndChoice(Member member, Choice choice);
}
