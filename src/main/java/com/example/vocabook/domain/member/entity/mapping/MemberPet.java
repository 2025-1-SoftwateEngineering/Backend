package com.example.vocabook.domain.member.entity.mapping;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Pet;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_pet")
public class MemberPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_pet_id")
    private Long id;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private Long currentLevel = 1L;

    @Column(name = "current_exp", nullable = false)
    @Builder.Default
    private Long currentExp = 0L;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}
