package com.example.vocabook.domain.store.repository;

import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByItemType(ItemType itemType);

	@Modifying
	@Query(value = "DELETE FROM item WHERE item_type IN ('PET_FOOD_PREMIUM', 'PET_FOOD_BASIC')", nativeQuery = true)
	void deleteObsoleteItems();

    boolean existsByItemTypeAndName(ItemType itemType, String name);

    Item findByName(String name);
}
