package com.example.vocabook.global.scheduler;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.global.util.StreakScheduler;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.google.cloud.storage.Storage;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@DisplayName("StreakScheduler 비즈니스 로직 통합 테스트")
class StreakSchedulerTest {

    @Autowired private StreakScheduler streakScheduler;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AlertRepository alertRepository;
    @Autowired private AlertDetailRepository alertDetailRepository;
    @Autowired private MemberItemRepository memberItemRepository;
    @PersistenceContext private EntityManager em;

    // Quartz 실제 등록은 AlertScheduleServiceTest에서 별도 검증 → 여기선 Mock 처리
    @MockitoBean private AlertScheduleService alertScheduleService;
    // Firebase는 실제 초기화가 안 되므로 Mock 처리
    @MockitoBean private FirebaseMessaging firebaseMessaging;
    @MockitoBean private Storage storage;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private Member memberWithStreak;     // 어제 이전 학습 → 스트릭 초기화 대상
    private Member memberStudiedToday;   // 오늘 학습 → 스트릭 유지
    private Member memberWithZeroStreak; // 스트릭 원래 0 → 알림 대상 아님

    @BeforeEach
    void setUp() {
        // 1. 스트릭 5, 마지막 학습이 이틀 전인 사람 → 자정에 스트릭 깨져야 함
        memberWithStreak = memberRepository.save(Member.builder()
                .email("break@test.com")
                .password("pw")
                .nickname("스트릭깨질사람")
                .streak(5L)
                .lastStudiedAt(LocalDate.now(ZONE).minusDays(2))
                .refreshToken("token1")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberWithStreak)
                .fcmToken("fcm-token-1")
                .build());

        // 2. 오늘 학습한 사람 → 스트릭 유지
        memberStudiedToday = memberRepository.save(Member.builder()
                .email("today@test.com")
                .password("pw")
                .nickname("오늘학습자")
                .streak(3L)
                .lastStudiedAt(LocalDate.now(ZONE))
                .refreshToken("token2")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberStudiedToday)
                .fcmToken("fcm-token-2")
                .build());

        // 3. 처음부터 스트릭이 0인 사람 → 알림 대상 아님
        memberWithZeroStreak = memberRepository.save(Member.builder()
                .email("zero@test.com")
                .password("pw")
                .nickname("스트릭없는사람")
                .streak(0L)
                .lastStudiedAt(LocalDate.now(ZONE).minusDays(10))
                .refreshToken("token3")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberWithZeroStreak)
                .fcmToken("fcm-token-3")
                .build());
    }

    @Test
    @DisplayName("[정상] 어제 이전에 학습한 사람의 스트릭이 0으로 초기화된다")
    void resetStreak_shouldBreakStreak() throws Exception {
        // when
        streakScheduler.resetStreak();

        // then
        Member updated = memberRepository.findById(memberWithStreak.getId()).orElseThrow();
        assertThat(updated.getStreak()).isEqualTo(0L);
    }

    @Test
    @DisplayName("[정상] 오늘 학습한 사람의 스트릭은 초기화되지 않는다")
    void resetStreak_shouldMaintainStreakForTodayLearner() throws Exception {
        // when
        streakScheduler.resetStreak();

        // then
        Member updated = memberRepository.findById(memberStudiedToday.getId()).orElseThrow();
        assertThat(updated.getStreak()).isEqualTo(3L); // 변동 없음
    }

    @Test
    @DisplayName("[정상] 스트릭이 깨진 사람에게만 AlertDetail이 생성되고 schedule()이 호출된다")
    void resetStreak_shouldCreateAlertOnlyForTargetMember() throws Exception {
        // when
        streakScheduler.resetStreak();

        // then - AlertDetail 1건만 생성 (memberWithStreak만 대상)
        List<AlertDetail> details = alertDetailRepository.findAll();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getContent()).isNotBlank();

        // AlertScheduleService.schedule()이 정확히 1번 호출되었는지 확인
        verify(alertScheduleService, times(1)).schedule(any(AlertDetail.class));
    }

    @Test
    @DisplayName("[경계값] 처음부터 스트릭이 0인 사람은 알림 대상에 포함되지 않는다")
    void resetStreak_shouldNotAlertMemberWithAlreadyZeroStreak() throws Exception {
        // when
        streakScheduler.resetStreak();

        // then - memberWithZeroStreak은 알림 대상이 아님
        List<AlertDetail> details = alertDetailRepository.findAll();
        boolean isZeroStreakMemberIncluded = details.stream()
                .anyMatch(d -> d.getAlert().getMember().getId().equals(memberWithZeroStreak.getId()));
        assertThat(isZeroStreakMemberIncluded).isFalse();
    }

    @Test
    @DisplayName("[엣지] 스트릭프리즈 아이템 보유 시 스트릭 유지되고 아이템만 삭제된다")
    void resetStreak_shouldKeepStreakAndDeleteItemWhenStreakFreezeExists() throws Exception {
        // given - 스트릭 5, 이틀 전 학습 → 원래라면 스트릭 깨져야 하는 대상
        Member memberWithFreeze = memberRepository.save(Member.builder()
                .email("freeze@test.com")
                .password("pw")
                .nickname("프리즈보유자")
                .streak(5L)
                .lastStudiedAt(LocalDate.now(ZONE).minusDays(2))
                .refreshToken("token4")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberWithFreeze)
                .fcmToken("fcm-token-4")
                .build());

        // 스트릭프리즈 아이템 생성 후 연결
        Item freezeItem = Item.builder()
                .name("스트릭프리즈")
                .price(500L)
                .itemType(ItemType.STREAK_FREEZE)
                .build();
        em.persist(freezeItem);

        MemberItem memberItem = memberItemRepository.save(MemberItem.builder()
                .member(memberWithFreeze)
                .item(freezeItem)
                .build());

        // when
        streakScheduler.resetStreak();

        // then - 스트릭 유지
        Member updated = memberRepository.findById(memberWithFreeze.getId()).orElseThrow();
        assertThat(updated.getStreak()).isEqualTo(5L);

        // then - 아이템 삭제됨
        assertThat(memberItemRepository.findById(memberItem.getId())).isEmpty();
    }
}
