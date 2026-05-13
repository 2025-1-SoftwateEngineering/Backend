package com.example.vocabook.global.util;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StreakScheduler {

    private final MemberRepository memberRepository;
    private final MemberItemRepository memberItemRepository;

    // streak 리셋
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetStreak() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

        List<Member> members = memberRepository.findStreakResetTargets(yesterday);

        for (Member member : members) {
            memberItemRepository.findFirstByMemberAndItem_ItemType(member, ItemType.STREAK_FREEZE)
                    .ifPresentOrElse(
                            memberItemRepository::delete,
                            member::resetStreak
                    );
        }
    }
}
