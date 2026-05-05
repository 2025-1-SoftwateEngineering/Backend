package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.global.util.AlertScheduler;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertScheduleService {

    private final Scheduler scheduler;

    public void schedule(
            AlertDetail alertDetail
    ) throws SchedulerException {
        // JobDetail 생성: 어떤 Job을 실행할 것인지, 어떤 데이터를 넘길 것인지 정의
        JobDetail jobDetail = JobBuilder.newJob(AlertScheduler.class)
                .withIdentity(alertDetail.getId().toString(), "ALARM_GROUP") // 알람 ID를 식별자로 사용
                .usingJobData("alertId", alertDetail.getAlert().getId())
                .usingJobData("content", alertDetail.getContent())
                .usingJobData("repeat", alertDetail.getRepeat().toString())
                .build();

        // Trigger 생성: 언제 실행할 것인지 (Cron 표현식 사용)
        String cronExpression = generateCronExpression(alertDetail);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(alertDetail.getId().toString(), "ALARM_GROUP")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        // Quartz 스케줄러에 등록
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void deleteAlarm(Long alarmId) throws SchedulerException {
        JobKey jobKey = new JobKey(alarmId.toString(), "ALARM_GROUP");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }

    // 3. 알람 스케줄 수정 (기존 것 삭제 후 재등록)
    public void updateAlarm(AlertDetail alarm) throws SchedulerException {
        deleteAlarm(alarm.getId());
        schedule(alarm);
    }

    // 알람 타입에 맞춰 Cron 표현식 생성
    private String generateCronExpression(AlertDetail alarm) {
        Integer hour = alarm.getAlertedAt().getHour();
        Integer minute = alarm.getAlertedAt().getMinute();
        Integer second = 0;

        switch (alarm.getRepeat()) {
            case DAY:
                // 매일 특정 시간 (예: 0 30 08 * * ?)
                return String.format("%d %d %d * * ?", second, minute, hour);

            case WEEK:
                String dayOfWeek = alarm.getAlertedAt().getDayOfWeek().toString().toUpperCase().substring(0, 3);
                // 매주 특정 요일 (예: 0 30 08 ? * MON,WED,FRI)
                return String.format("%d %d %d ? * %s", second, minute, hour, dayOfWeek);

            case NONE:
                // 특정 날짜 1회 (예: 0 30 08 14 04 ? 2026)
                Integer year = alarm.getAlertedAt().getYear();
                Integer month = alarm.getAlertedAt().getMonthValue();
                Integer day = alarm.getAlertedAt().getDayOfMonth();
                return String.format("%s %s %s %s %s ? %s", second, minute, hour, day, month, year);

            default:
                throw new AlertException(AlertErrorCode.INVADED_ALERT_TYPE);
        }
    }
}
