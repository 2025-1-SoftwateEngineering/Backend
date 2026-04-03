package com.example.vocabook.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Long level = 1L;

    @Column(name = "required_exp", nullable = false)
    @Builder.Default
    private Long requiredExp = 100L;
}
