package com.example.vocabook.global.scheduler;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.util.ReviewScheduler;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@DisplayName("ReviewScheduler 비즈니스 로직 통합 테스트")
class ReviewSchedulerTest {

    @Autowired private ReviewScheduler reviewScheduler;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AlertRepository alertRepository;
    @Autowired private AlertDetailRepository alertDetailRepository;

    // Quartz 실제 등록은 AlertScheduleServiceTest에서 별도 검증 → 여기선 Mock 처리
    @MockitoBean private AlertScheduleService alertScheduleService;
    // Firebase는 실제 초기화가 안 되므로 Mock 처리
    @MockitoBean private FirebaseMessaging firebaseMessaging;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private Member memberStudiedYesterday; // 알림 대상 (어제 학습, 오늘 아직 안 함)
    private Member memberStudiedToday;     // 알림 대상 아님 (오늘 이미 학습)
    private Member memberNeverStudied;     // 알림 대상 아님 (학습 이력 없음)

    @BeforeEach
    void setUp() {
        // 1. 어제 마지막으로 학습한 사람 (오늘 학습 독려 알림 대상)
        memberStudiedYesterday = memberRepository.save(Member.builder()
                .email("yesterday@test.com")
                .password("pw")
                .nickname("어제학습자")
                .streak(3L)
                .lastStudiedAt(LocalDate.now(ZONE).minusDays(1))
                .refreshToken("token1")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberStudiedYesterday)
                .fcmToken("fcm-token-1")
                .build());

        // 2. 오늘 이미 학습을 마친 사람 (알림 불필요)
        memberStudiedToday = memberRepository.save(Member.builder()
                .email("today@test.com")
                .password("pw")
                .nickname("오늘학습자")
                .streak(5L)
                .lastStudiedAt(LocalDate.now(ZONE))
                .refreshToken("token2")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberStudiedToday)
                .fcmToken("fcm-token-2")
                .build());

        // 3. 학습 이력이 전혀 없는 사람 (알림 대상 아님)
        memberNeverStudied = memberRepository.save(Member.builder()
                .email("none@test.com")
                .password("pw")
                .nickname("미학습자")
                .streak(0L)
                .lastStudiedAt(null)
                .refreshToken("token3")
                .build());
        alertRepository.save(Alert.builder()
                .member(memberNeverStudied)
                .fcmToken("fcm-token-3")
                .build());
    }

    @Test
    @DisplayName("[정상] 어제 학습한 사람에게 AlertDetail이 1건 생성되고 schedule()이 호출된다")
    void reviewScheduler_shouldCreateAlertForYesterdayLearner() throws Exception {
        // when
        reviewScheduler.reviewScheduler();

        // then
        List<AlertDetail> details = alertDetailRepository.findAll();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getAlert().getMember().getId())
                .isEqualTo(memberStudiedYesterday.getId());

        // AlertScheduleService.schedule()이 정확히 1번 호출되었는지 확인
        verify(alertScheduleService, times(1)).schedule(any(AlertDetail.class));
    }

    @Test
    @DisplayName("[정상] 생성된 알림 내용이 비어있지 않다")
    void reviewScheduler_shouldContainMessage() throws Exception {
        // when
        reviewScheduler.reviewScheduler();

        // then
        AlertDetail detail = alertDetailRepository.findAll().get(0);
        assertThat(detail.getContent()).isNotBlank();
    }

    @Test
    @DisplayName("[경계값] 오늘 이미 학습한 사람은 알림 대상에서 제외된다")
    void reviewScheduler_shouldExcludeTodayLearner() throws Exception {
        // when
        reviewScheduler.reviewScheduler();

        // then
        List<AlertDetail> details = alertDetailRepository.findAll();
        boolean isTodayLearnerIncluded = details.stream()
                .anyMatch(d -> d.getAlert().getMember().getId().equals(memberStudiedToday.getId()));
        assertThat(isTodayLearnerIncluded).isFalse();
    }

    @Test
    @DisplayName("[경계값] 학습 이력이 없는 사람은 알림 대상에서 제외된다")
    void reviewScheduler_shouldExcludeMemberWithNoStudyHistory() throws Exception {
        // when
        reviewScheduler.reviewScheduler();

        // then
        List<AlertDetail> details = alertDetailRepository.findAll();
        boolean isNeverStudiedIncluded = details.stream()
                .anyMatch(d -> d.getAlert().getMember().getId().equals(memberNeverStudied.getId()));
        assertThat(isNeverStudiedIncluded).isFalse();
    }
}
