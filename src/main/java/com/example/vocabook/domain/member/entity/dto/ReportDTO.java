package com.example.vocabook.domain.member.entity.dto;

import com.example.vocabook.domain.member.entity.Report;
import lombok.Builder;

public class ReportDTO {

    @Builder
    public record ReportCnt(
            Report report,
            Long cnt
    ) {}
}
