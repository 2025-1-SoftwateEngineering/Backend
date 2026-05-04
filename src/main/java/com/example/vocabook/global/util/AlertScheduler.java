package com.example.vocabook.global.util;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler extends QuartzJobBean {

    private final FcmUtil fcmUtil;
    private final AlertDetailRepository alertDetailRepository;
    private final AlertRepository alertRepository;

    @Override
    @Transactional
    protected void executeInternal(
            JobExecutionContext context
    ) throws JobExecutionException {

        log.info("[Job 스케쥴러]: 시작...");
        JobDataMap dataMap = context.getMergedJobDataMap();
        Alert alert = alertRepository.findById(dataMap.getLong("alertId"))
                .orElseThrow(() -> new AlertException(AlertErrorCode.NOT_FOUND_FCM));

        try {
            fcmUtil.sendAlert("보카버디", dataMap.getString("content"), alert.getFcmToken());
        } catch (FirebaseMessagingException e) {
            log.error("[FCM 전송]: 전송 실패");
        }
        log.info("[Job 스케쥴러]: 알림 전송 완료");

        // 일회성 알람이면 삭제
        if (dataMap.getString("repeat").equals(Repeat.NONE.name())){
            alertDetailRepository.deleteById(Long.parseLong(context.getJobDetail().getKey().getName()));
        }
    }
}
