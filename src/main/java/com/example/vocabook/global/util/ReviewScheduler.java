package com.example.vocabook.global.util;

import com.example.vocabook.domain.alert.converter.AlertConverter;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewScheduler {

    private final AlertRepository alertRepository;
    private final AlertScheduleService alertSchedulerService;
    private final AlertDetailRepository alertDetailRepository;

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    // 스트릭 깨지는거 방지 알림 (어제 학습한 사람)
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    @Transactional
    public void reviewScheduler() {

        // 마지막 학습일자가 어제인 사용자 조회
        // AlertDetail 생성
        List<Alert> alertList = alertRepository
                .findAllByMember_LastStudiedAt(LocalDate.now(zoneId).minusDays(1));

        // 5분 후 울리도록 설정
        List<AlertDetail> alertDetailList = alertList.stream()
                .map(i -> AlertConverter
                        .toAlertDetail(
                                i,
                                OffsetDateTime.now(zoneId).plusMinutes(5).withSecond(0).withNano(0),
                                Repeat.NONE,
                                "오늘 남은 시간이 얼마 남지 않았아요!! 연속 학습 일자가 깨지지 않도록 지금 학습하세요!"
                        )
                )
                .toList();

        // DB에 저장
        alertDetailRepository.saveAll(alertDetailList);

        // 알림 스케쥴러 삽입
        alertDetailList
                .forEach(i -> {
                    try {
                        alertSchedulerService.schedule(i);
                    } catch (SchedulerException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
