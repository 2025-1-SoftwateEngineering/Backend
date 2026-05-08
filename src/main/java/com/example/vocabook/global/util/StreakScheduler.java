package com.example.vocabook.global.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class StreakScheduler {

    @PersistenceContext
    private EntityManager entityManager;

    // 매일 자정에 실행 — 어제 이전 마지막 학습자 streak 리셋
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetStreak() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

        entityManager.createQuery(
                "UPDATE Member m SET m.streak = 0 " +
                "WHERE m.lastStudiedAt < :yesterday AND m.streak > 0"
        )
        .setParameter("yesterday", yesterday)
        .executeUpdate();
    }
}