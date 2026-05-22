package com.example.vocabook.global.scheduler;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.google.cloud.storage.Storage;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AlertScheduleService Quartz 실제 등록 여부 통합 테스트
 *
 * [주의] 이 테스트는 AlertScheduleService를 실제로 주입받으므로
 * AlertScheduleService를 @MockitoBean으로 처리하는 Streak/ReviewSchedulerTest와
 * 스프링 컨텍스트가 다릅니다. Suite에 포함되지 않으며 단독으로 실행하세요.
 * 실행: ./gradlew test --tests "com.example.vocabook.global.scheduler.AlertScheduleServiceTest"
 */
@SpringBootTest
@Transactional
@DisplayName("AlertScheduleService Quartz 등록 통합 테스트")
class AlertScheduleServiceTest {

    @Autowired private AlertScheduleService alertScheduleService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AlertRepository alertRepository;
    @Autowired private AlertDetailRepository alertDetailRepository;
    @Autowired private Scheduler quartzScheduler; // 실제 Quartz Scheduler 주입

    // Firebase는 실제 초기화가 안 되므로 Mock 처리
    @MockitoBean private FirebaseMessaging firebaseMessaging;
    @MockitoBean private Storage storage;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final String ALARM_GROUP = "ALARM_GROUP";

    private Alert testAlert;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .email("quartz@test.com")
                .password("pw")
                .nickname("쿼츠테스터")
                .streak(0L)
                .refreshToken("token")
                .build());
        testAlert = alertRepository.save(Alert.builder()
                .member(member)
                .fcmToken("quartz-fcm-token")
                .build());
    }

    @Test
    @DisplayName("[NONE] 일회성 알림을 등록하면 QRTZ_TRIGGERS 테이블에 Job이 생성된다")
    void schedule_none_shouldRegisterQuartzJob() throws Exception {
        // given - 반드시 미래 시간으로 설정
        AlertDetail alertDetail = alertDetailRepository.save(AlertDetail.builder()
                .alert(testAlert)
                .content("일회성 테스트 알림")
                .repeat(Repeat.NONE)
                .alertedAt(LocalDateTime.now(ZONE).plusHours(1))
                .build());

        // when
        alertScheduleService.schedule(alertDetail);

        // then - Quartz에 해당 Job이 실제로 등록되었는지 확인
        JobKey jobKey = new JobKey(alertDetail.getId().toString(), ALARM_GROUP);
        assertThat(quartzScheduler.checkExists(jobKey)).isTrue();
    }

    @Test
    @DisplayName("[DAY] 매일 반복 알림을 등록하면 QRTZ_TRIGGERS 테이블에 Job이 생성된다")
    void schedule_day_shouldRegisterQuartzJob() throws Exception {
        // given
        AlertDetail alertDetail = alertDetailRepository.save(AlertDetail.builder()
                .alert(testAlert)
                .content("매일 반복 알림")
                .repeat(Repeat.DAY)
                .alertedAt(LocalDateTime.now(ZONE).plusHours(1))
                .build());

        // when
        alertScheduleService.schedule(alertDetail);

        // then
        JobKey jobKey = new JobKey(alertDetail.getId().toString(), ALARM_GROUP);
        assertThat(quartzScheduler.checkExists(jobKey)).isTrue();
    }

    @Test
    @DisplayName("[WEEK] 매주 반복 알림을 등록하면 QRTZ_TRIGGERS 테이블에 Job이 생성된다")
    void schedule_week_shouldRegisterQuartzJob() throws Exception {
        // given
        AlertDetail alertDetail = alertDetailRepository.save(AlertDetail.builder()
                .alert(testAlert)
                .content("매주 반복 알림")
                .repeat(Repeat.WEEK)
                .alertedAt(LocalDateTime.now(ZONE).plusHours(1))
                .build());

        // when
        alertScheduleService.schedule(alertDetail);

        // then
        JobKey jobKey = new JobKey(alertDetail.getId().toString(), ALARM_GROUP);
        assertThat(quartzScheduler.checkExists(jobKey)).isTrue();
    }

    @Test
    @DisplayName("[삭제] 등록된 알림을 삭제하면 QRTZ_TRIGGERS에서 제거된다")
    void deleteAlarm_shouldRemoveQuartzJob() throws Exception {
        // given - 먼저 등록
        AlertDetail alertDetail = alertDetailRepository.save(AlertDetail.builder()
                .alert(testAlert)
                .content("삭제 테스트 알림")
                .repeat(Repeat.NONE)
                .alertedAt(LocalDateTime.now(ZONE).plusHours(2))
                .build());
        alertScheduleService.schedule(alertDetail);

        // 등록 확인
        JobKey jobKey = new JobKey(alertDetail.getId().toString(), ALARM_GROUP);
        assertThat(quartzScheduler.checkExists(jobKey)).isTrue();

        // when - 삭제
        alertScheduleService.deleteAlarm(alertDetail.getId());

        // then - Quartz에서 제거되었는지 확인
        assertThat(quartzScheduler.checkExists(jobKey)).isFalse();
    }

    @Test
    @DisplayName("[Trigger] 등록된 알림의 Cron Trigger가 타임존(Asia/Seoul)으로 설정된다")
    void schedule_shouldConfigureTriggerWithSeoulTimezone() throws Exception {
        // given
        AlertDetail alertDetail = alertDetailRepository.save(AlertDetail.builder()
                .alert(testAlert)
                .content("타임존 확인 알림")
                .repeat(Repeat.DAY)
                .alertedAt(LocalDateTime.now(ZONE).plusHours(1))
                .build());

        // when
        alertScheduleService.schedule(alertDetail);

        // then - Trigger 타임존 확인
        TriggerKey triggerKey = new TriggerKey(alertDetail.getId().toString(), ALARM_GROUP);
        org.quartz.CronTrigger trigger =
                (org.quartz.CronTrigger) quartzScheduler.getTrigger(triggerKey);

        assertThat(trigger).isNotNull();
        assertThat(trigger.getTimeZone().getID()).isEqualTo("Asia/Seoul");
    }
}
