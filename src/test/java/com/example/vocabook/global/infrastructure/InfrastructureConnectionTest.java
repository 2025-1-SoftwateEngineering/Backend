package com.example.vocabook.global.infrastructure;

import com.example.vocabook.global.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.cloud.storage.Storage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class InfrastructureConnectionTest {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Scheduler scheduler;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private Storage storage;

    @Test
    @DisplayName("인프라 점검: Redis 연결 및 데이터 읽기/쓰기 정상 작동 확인")
    void verifyRedisConnection() {
        // given
        String testKey = "infra:test:key";
        String testValue = "testValue123";

        // when
        redisUtil.save(testKey, testValue, java.time.Duration.ofSeconds(10));
        
        // then
        assertThat(redisUtil.hasKey(testKey)).isTrue();
        
        String retrievedValue = (String) redisUtil.get(testKey);
        assertThat(retrievedValue).isEqualTo(testValue);

        // cleanup
        redisUtil.delete(testKey);
        assertThat(redisUtil.hasKey(testKey)).isFalse();
    }

    @Test
    @DisplayName("인프라 점검: Spring Quartz 스케줄러 정상 구동 및 Job 등록 확인")
    void verifyQuartzScheduler() throws SchedulerException, InterruptedException {
        // 1. 스케줄러 객체가 존재하고 시작된 상태인지 확인
        assertThat(scheduler).isNotNull();
        assertThat(scheduler.isStarted()).isTrue();

        // 2. 더미 Job이 정상적으로 스케줄링되고 실행되는지 확인을 위한 래치
        CountDownLatch latch = new CountDownLatch(1);
        scheduler.getContext().put("latch", latch);

        JobDetail job = JobBuilder.newJob(TestJob.class)
                .withIdentity("testJob", "infraGroup")
                .storeDurably() // 트리거가 끝나도 Job이 자동 삭제되지 않도록 설정 (안전한 클린업을 위해)
                .build();

        // 1초 뒤에 1번만 실행되는 트리거 생성
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testTrigger", "infraGroup")
                .startAt(new java.util.Date(System.currentTimeMillis() + 100)) 
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                .build();

        // when
        scheduler.scheduleJob(job, trigger);

        // then
        // 래치가 카운트다운 될 때까지 최대 2초 대기 (Job이 정상적으로 실행되어야 카운트다운됨)
        boolean executed = latch.await(2, TimeUnit.SECONDS);
        assertThat(executed).as("Quartz Job이 정상적으로 스케줄링되고 실행되어야 합니다.").isTrue();
        
        // cleanup
        try {
            if (scheduler.checkExists(job.getKey())) {
                scheduler.deleteJob(job.getKey());
            }
        } catch (SchedulerException e) {
            // 트리거 실행 후 이미 정리된 경우 무시
            System.out.println("Cleanup ignore: " + e.getMessage());
        }
    }

    // Quartz 실행을 확인하기 위한 더미 Job 클래스
    public static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                CountDownLatch latch = (CountDownLatch) context.getScheduler().getContext().get("latch");
                if (latch != null) {
                    latch.countDown();
                }
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
