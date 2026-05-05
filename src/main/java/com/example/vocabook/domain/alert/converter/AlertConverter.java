package com.example.vocabook.domain.alert.converter;

import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.member.entity.Member;

import java.time.LocalDateTime;

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
            AlertReqDTO.CustomAlert dto
    ){
        return AlertDetail.builder()
                .alertedAt(dto.alertedAt().atZoneSameInstant(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .content(dto.message())
                .repeat(dto.repeat())
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
}
