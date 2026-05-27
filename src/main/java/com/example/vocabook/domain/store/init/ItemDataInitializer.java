package com.example.vocabook.domain.store.init;

import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemDataInitializer implements ApplicationRunner {

	private final ItemRepository itemRepository;

	private record ItemDef(ItemType type, String name, long price) {}

	private static final List<ItemDef> ITEM_DEFINITIONS = List.of(
			new ItemDef(ItemType.STREAK_FREEZE,         "연속학습 파괴 방어권",   500L),
			new ItemDef(ItemType.PET_FOOD,              "사료",                  80L),
			new ItemDef(ItemType.PET_WATER,             "물",                    50L),
			new ItemDef(ItemType.CHOICE_TIME_10,        "사지선다 시간 +10초",   100L),
			new ItemDef(ItemType.CHOICE_TIME_30,        "사지선다 시간 +30초",   200L),
			new ItemDef(ItemType.CROSSWORD_HINT_START,  "십자말풀이 시작 힌트",  180L),
			new ItemDef(ItemType.CROSSWORD_HINT_MIDDLE, "십자말풀이 중간 힌트",  220L),
            new ItemDef(ItemType.PET_BG, "기본 펫 배경", 0L),
			new ItemDef(ItemType.PET_BG,              "펫 배경 1",             200L),
			new ItemDef(ItemType.PET_BG,              "펫 배경 2",             500L),
            new ItemDef(ItemType.PROFILE_PHOTO, "기본 프로필 사진", 0L),
			new ItemDef(ItemType.PROFILE_PHOTO,       "프로필 사진 1",         300L),
			new ItemDef(ItemType.PROFILE_PHOTO,       "프로필 사진 2",         300L),
            new ItemDef(ItemType.PROFILE_BG, "기본 프로필 배경", 0L),
			new ItemDef(ItemType.PROFILE_BG,          "프로필 배경 1",         200L),
			new ItemDef(ItemType.PROFILE_BG,          "프로필 배경 2",         200L)
	);

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		itemRepository.deleteObsoleteItems();

		for (ItemDef def : ITEM_DEFINITIONS) {

			if (!itemRepository.existsByItemTypeAndName(def.type(), def.name())) {
                Item.ItemBuilder item = Item.builder()
                        .name(def.name())
                        .price(def.price())
                        .itemType(def.type());

                if (def.name().equals("기본 펫 배경")){
                    item.imageUrl("https://storage.googleapis.com/vocabuddy-storage/pet/default-pet.jpeg");
                }

                if (def.name().equals("기본 프로필 사진")){
                    item.imageUrl("https://storage.googleapis.com/vocabuddy-storage/profile/defalut_profile.png");
                }

                if (def.name().equals("기본 프로필 배경")){
                    item.imageUrl("https://storage.googleapis.com/vocabuddy-storage/background/default_bg.png");
                }

				itemRepository.save(item.build());
			}
		}
	}
}
