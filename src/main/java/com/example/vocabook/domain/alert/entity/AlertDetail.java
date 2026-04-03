package com.example.vocabook.domain.alert.entity;

import com.example.vocabook.domain.alert.enums.Repeat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "alert_detail")
public class AlertDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_detail_id")
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "send_repeat", nullable = false)
    @Enumerated(EnumType.STRING)
    private Repeat repeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;
}
