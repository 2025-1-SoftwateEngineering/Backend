package com.example.vocabook.domain.alert.dto;

import com.example.vocabook.domain.alert.enums.Repeat;
import lombok.Builder;

import java.time.LocalDateTime;

public class AlertResDTO {

    @Builder
    public record RegisterFcm(
            Long memberId,
            LocalDateTime registeredAt
    ) {}

    @Builder
    public record CustomAlert(
            String message,
            Repeat repeat,
            LocalDateTime alertedAt
    ) {}
}
