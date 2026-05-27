package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.entity.PetImage;
import com.example.vocabook.domain.member.repository.PetImageRepository;
import com.example.vocabook.domain.pet.enums.PetStage;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetImageInitializer implements CommandLineRunner {

    private final PetImageRepository petImageRepository;
    private final ItemRepository itemRepository;

    public static final String DEFAULT_BG_NAME = "기본 펫 배경";
    public static final String DEFAULT_ACCESSORY_NAME = "기본 펫 악세서리";

    @Override
    public void run(String... args) throws Exception {
        // 1. 펫 단계별 기본 이미지 세팅
        for (PetStage stage : PetStage.values()) {
            if (petImageRepository.findByPetStage(stage).isEmpty()) {
                petImageRepository.save(PetImage.builder()
                        .petStage(stage)
                        .imageUrl("") // 기본값 (추후 관리자 업로드로 업데이트됨)
                        .build());
            }
        }

        // 2. 펫 기본 배경화면 아이템 세팅
        if (!itemRepository.existsByItemTypeAndName(ItemType.PET_BG, DEFAULT_BG_NAME)) {
            itemRepository.save(Item.builder()
                    .name(DEFAULT_BG_NAME)
                    .itemType(ItemType.PET_BG)
                    .price(0L)
                    .imageUrl("")
                    .build());
        }

        // 3. 펫 기본 악세서리 아이템 세팅
        if (!itemRepository.existsByItemTypeAndName(ItemType.PET_ACCESSORY, DEFAULT_ACCESSORY_NAME)) {
            itemRepository.save(Item.builder()
                    .name(DEFAULT_ACCESSORY_NAME)
                    .itemType(ItemType.PET_ACCESSORY)
                    .price(0L)
                    .imageUrl("")
                    .build());
        }
    }
}
