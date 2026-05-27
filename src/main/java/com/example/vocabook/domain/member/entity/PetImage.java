package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.pet.enums.PetStage;
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
@Table(name = "pet_image")
public class PetImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_image_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_stage", nullable = false, unique = true)
    private PetStage petStage;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public void updateImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
}
