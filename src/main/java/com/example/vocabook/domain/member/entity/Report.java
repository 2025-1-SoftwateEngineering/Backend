package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.member.enums.ReportReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "report_reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reportReason;

    @Column(name = "detail_reason", nullable = false)
    private String detailReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private Member targetMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_member_id", nullable = false)
    private Member reportMember;
}
