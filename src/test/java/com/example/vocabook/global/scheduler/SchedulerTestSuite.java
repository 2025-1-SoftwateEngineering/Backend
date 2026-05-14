package com.example.vocabook.global.scheduler;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * 스케줄러 비즈니스 로직 테스트를 한 번에 실행하는 Suite 클래스
 *
 * [포함된 테스트]
 * - StreakSchedulerTest : 스트릭 초기화 대상 선별, DB 업데이트, 알림 생성 검증
 * - ReviewSchedulerTest : 학습 독려 알림 대상 선별, 알림 생성 검증
 *
 * [별도 실행 필요]
 * - AlertScheduleServiceTest : 실제 Quartz Scheduler를 주입받아 QRTZ_TRIGGERS 등록 여부를 직접 검증
 *   → @MockitoBean AlertScheduleService를 사용하는 위 두 테스트와 스프링 컨텍스트가 달라 Suite에서 제외
 *   → 실행: ./gradlew test --tests "com.example.vocabook.global.scheduler.AlertScheduleServiceTest"
 */
@Suite
@SelectClasses({
        StreakSchedulerTest.class,
        ReviewSchedulerTest.class
})
public class SchedulerTestSuite {
}
