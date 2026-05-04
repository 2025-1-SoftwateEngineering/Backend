package com.example.vocabook.domain.alert.dto;

import com.example.vocabook.domain.alert.enums.Repeat;

import java.time.OffsetDateTime;

public class AlertReqDTO {

    public record RegisterFcm(
            String fcmToken
    ) {}

    public record CustomAlert(
            String message,
            Repeat repeat,
            OffsetDateTime alertedAt
    ) {}
}
