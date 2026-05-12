package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.enums.ReportReason;

public class ReportConverter {

    public static Report toReport(
            Member targetMember,
            Member reportMember,
            ReportReason reportReason,
            String detailReason
    ){
        return Report.builder()
                .reportReason(reportReason)
                .detailReason(detailReason)
                .targetMember(targetMember)
                .reportMember(reportMember)
                .build();
    }
}
