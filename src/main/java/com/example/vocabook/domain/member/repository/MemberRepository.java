package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findByIdWithLock(Long id);

    // streak 리셋 대상 조회
    @Query("SELECT m FROM Member m WHERE m.streak > 0 AND m.lastStudiedAt < :yesterday")
    List<Member> findStreakResetTargets(LocalDate yesterday);
}
