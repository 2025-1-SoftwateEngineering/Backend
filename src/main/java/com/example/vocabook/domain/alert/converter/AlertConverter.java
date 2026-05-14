package com.example.vocabook.domain.alert.converter;

import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class AlertConverter {

    public static Alert toAlert(
            String fcmToken,
            Member member
    ) {
        return Alert.builder()
                .fcmToken(fcmToken)
                .member(member)
                .build();
    }

    public static AlertDetail toAlertDetail(
            Alert alert,
            OffsetDateTime alertedAt,
            Repeat repeat,
            String message
    ){
        return AlertDetail.builder()
                .alertedAt(alertedAt.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .content(message)
                .repeat(repeat)
                .alert(alert)
                .build();
    }

    // FCM 토큰 저장
    public static AlertResDTO.RegisterFcm toRegisterFcm(
            Alert alert
    ){
        return AlertResDTO.RegisterFcm.builder()
                .memberId(alert.getMember().getId())
                .registeredAt(LocalDateTime.now())
                .build();
    }

    // 알림 커스텀
    public static AlertResDTO.CustomAlert toCustomAlert(
            AlertDetail alertDetail
    ){
        return AlertResDTO.CustomAlert.builder()
                .alertedAt(alertDetail.getAlertedAt())
                .message(alertDetail.getContent())
                .repeat(alertDetail.getRepeat())
                .build();
    }

    // 알림 목록 조회
    public static AlertResDTO.AlertList toAlertList(
            AlertDetail alertDetail
    ){
        return AlertResDTO.AlertList.builder()
                .alertId(alertDetail.getId())
                .message(alertDetail.getContent())
                .alertedAt(alertDetail.getAlertedAt())
                .repeat(alertDetail.getRepeat())
                .build();
    }

    // 알림 삭제
    public static AlertResDTO.DeleteAlert toDeleteAlert(
            AlertDetail alertDetail
    ){
        return AlertResDTO.DeleteAlert.builder()
                .alertId(alertDetail.getId())
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
