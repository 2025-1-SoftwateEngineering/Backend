package com.example.vocabook.global.util;

import com.example.vocabook.domain.alert.converter.AlertConverter;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StreakScheduler {

    private final MemberRepository memberRepository;
    private final AlertDetailRepository alertDetailRepository;
    private final AlertScheduleService alertScheduleService;
    private final AlertRepository alertRepository;

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    // 매일 자정에 실행 — 어제 이전 마지막 학습자 streak 리셋
    @SneakyThrows
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetStreak() {
        LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

        // 스트릭 초기화
        List<Member> memberList = memberRepository
                .findAllByLastStudiedAtBeforeAndStreakGreaterThan(yesterday, 0L);

        memberList.forEach(Member::breakStreak);

        // 알림 전송
        List<Alert> alertList = alertRepository.findAllByMemberIn(memberList);

        List<AlertDetail> alertDetailList = alertList.stream()
                .map(i -> AlertConverter
                        .toAlertDetail(
                                i,
                                OffsetDateTime.now(zoneId).withMinute(5).withSecond(0).withNano(0),
                                Repeat.NONE,
                                i.getMember().getNickname()+"님! 스트릭이 깨졌어요... 그렇지만 다시 학습을 시작해보는건 어떤가요?"
                        )
                )
                .toList();

        alertDetailRepository.saveAll(alertDetailList);

        alertDetailList.forEach(alertScheduleService::schedule);
    }
}