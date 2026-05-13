package com.example.vocabook.domain.voca.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "voca")
public class Voca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voca_id")
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "solved_coin", nullable = false)
    @Builder.Default
    private Long solvedCoin = 0L;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void update(String description, Long solvedCoin) {
        if (description != null) {
            this.description = description;
        }
        if (solvedCoin != null) {
            this.solvedCoin = solvedCoin;
        }
    }
}
