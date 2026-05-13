package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findAllByLastStudiedAtBeforeAndStreakGreaterThan(LocalDate lastStudiedAtBefore, Long streakIsGreaterThan);
}
