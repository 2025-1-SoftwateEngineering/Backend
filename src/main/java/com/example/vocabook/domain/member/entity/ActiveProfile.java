package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.store.enums.ItemType;
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
public class ActiveProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "active_profile_id")
    private Long id;

    @Column(name = "profile_url", nullable = false)
    private String profileUrl;

    @Column(name = "active_profile_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType activeProfileType;
}
