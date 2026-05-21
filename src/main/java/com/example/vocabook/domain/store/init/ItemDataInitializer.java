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
			new ItemDef(ItemType.PET_FOOD_BASIC,        "사료",                 100L),
			new ItemDef(ItemType.PET_WATER,             "물",                    65L),
			new ItemDef(ItemType.CHOICE_TIME_10,        "사지선다 시간 +10초",   100L),
			new ItemDef(ItemType.CHOICE_TIME_30,        "사지선다 시간 +30초",   200L),
			new ItemDef(ItemType.CROSSWORD_HINT_START,  "십자말풀이 시작 힌트",  180L),
			new ItemDef(ItemType.CROSSWORD_HINT_MIDDLE, "십자말풀이 중간 힌트",  220L),
			new ItemDef(ItemType.PET_BG_1,              "펫 배경 1",             200L),
			new ItemDef(ItemType.PET_BG_2,              "펫 배경 2",             500L)
	);

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		itemRepository.deleteObsoleteItems();

		for (ItemDef def : ITEM_DEFINITIONS) {
			if (!itemRepository.existsByItemType(def.type())) {
				itemRepository.save(Item.builder()
						.name(def.name())
						.price(def.price())
						.itemType(def.type())
						.build());
			}
		}
	}
}
