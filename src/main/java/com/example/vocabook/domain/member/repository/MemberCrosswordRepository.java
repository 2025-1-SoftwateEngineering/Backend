package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.voca.entity.Crossword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberCrosswordRepository extends JpaRepository<MemberCrossword, Long> {
    Long countByMemberAndCrossword(Member member, Crossword crossword);

    @Query(
            value = "select mc " +
                    "from MemberCrossword mc " +
                    "where mc.member = :member and mc.crossword = :crossword and mc.solvedAt is null " +
                    "order by mc.id asc " +
                    "limit 1"
    )
    Optional<MemberCrossword> findByMemberAndCrosswordAndSolvedAtIsNull(Member member, Crossword crossword);
}
