package com.example.vocabook.domain.alert.dto;

import com.example.vocabook.domain.alert.enums.Repeat;
import lombok.Builder;

import java.time.LocalDateTime;

public class AlertResDTO {

    // FCM 등록
    @Builder
    public record RegisterFcm(
            Long memberId,
            LocalDateTime registeredAt
    ) {}

    // 알림 커스텀
    @Builder
    public record CustomAlert(
            String message,
            Repeat repeat,
            LocalDateTime alertedAt
    ) {}

    // 알림 목록 조회
    @Builder
    public record AlertList(
            Long alertId,
            String message,
            Repeat repeat,
            LocalDateTime alertedAt
    ) {}

    // 알림 삭제
    @Builder
    public record DeleteAlert(
            Long alertId,
            LocalDateTime deletedAt
    ) {}
}
