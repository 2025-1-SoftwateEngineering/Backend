package com.example.vocabook.global.scheduler;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        StreakSchedulerTest.class,
        ReviewSchedulerTest.class,
        AlertScheduleServiceTest.class
})
public class SchedulerTestSuite {
    // 모든 스케줄러 관련 테스트를 한 번에 실행하는 Suite 클래스
}
