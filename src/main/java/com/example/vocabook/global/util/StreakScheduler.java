package com.example.vocabook.global.util;

import com.example.vocabook.domain.alert.converter.AlertConverter;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.RequiredArgsConstructor;
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
    private final MemberItemRepository memberItemRepository;
    private final AlertDetailRepository alertDetailRepository;
    private final AlertScheduleService alertScheduleService;
    private final AlertRepository alertRepository;

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    // streak 리셋
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetStreak() {
        LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);

        List<Member> members = memberRepository.findStreakResetTargets(yesterday);
        List<Alert> alertList = alertRepository.findAllByMemberIn(members);

        for (Member member : members) {
            memberItemRepository.findFirstByMemberAndItem_ItemType(member, ItemType.STREAK_FREEZE)
                    .ifPresentOrElse(
                            memberItemRepository::delete,
                            member::resetStreak
                    );
        }

        List<AlertDetail> alertDetailList = alertList.stream()
                .map(i -> AlertConverter
                        .toAlertDetail(
                                i,
                                OffsetDateTime.now(zoneId).plusMinutes(5).withSecond(0).withNano(0),
                                Repeat.NONE,
                                i.getMember().getNickname()+"님! 스트릭이 깨졌어요... 그렇지만 다시 학습을 시작해보는건 어떤가요?"
                        )
                )
                .toList();

        alertDetailRepository.saveAll(alertDetailList);

        alertDetailList.forEach(i -> {
            try {
                alertScheduleService.schedule(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}