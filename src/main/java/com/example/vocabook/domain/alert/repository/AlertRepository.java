package com.example.vocabook.domain.alert.repository;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByMember(Member member);
}
