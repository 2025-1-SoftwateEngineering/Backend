package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.converter.AlertConverter;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.alert.service.AlertScheduleService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MemberAlertService {

    private final AlertRepository alertRepository;
    private final AlertDetailRepository alertDetailRepository;
    private final AlertScheduleService alertScheduleService;

    @Async
    public void sendFriendRequestAlert(
            AuthMember auth,
            Member friend
    ){
        // 친구 FCM 토큰 조회
        Alert alert = alertRepository.findByMember(friend)
                .orElseThrow(() -> new MemberException(AlertErrorCode.NOT_FOUND_FCM));

        // AlertDetail 무조건 새로 제작 (기존 알람과 충돌 방지)
        AlertDetail alertDetail = alertDetailRepository.save(
                AlertConverter.toAlertDetail(
                        alert,
                        OffsetDateTime.now().plusMinutes(1),
                        Repeat.NONE,
                        auth.getMember().getNickname()+"님이 친구 요청을 보냈습니다. 확인해주세요!"
                )
        );

        // 알림 스케쥴러 삽입
        try {
            alertScheduleService.schedule(alertDetail);
        } catch (SchedulerException e) {
            throw new MemberException(AlertErrorCode.FAILED_SET_ALERT);
        }
    }
}
