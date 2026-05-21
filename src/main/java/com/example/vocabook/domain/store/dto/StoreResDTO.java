package com.example.vocabook.domain.store.dto;

import com.example.vocabook.domain.store.enums.ItemType;
import lombok.Builder;

import java.util.List;

public class StoreResDTO {

	@Builder
	public record ItemInfo(
			Long itemId,
			String name,
			Long price,
			ItemType itemType
	) {
	}

	@Builder
	public record ItemList(
			List<ItemInfo> items,
			int totalCount
	) {
	}

	@Builder
	public record MyItemInfo(
			Long memberItemId,
			ItemInfo item
	) {
	}

	@Builder
	public record MyItemList(
			List<MyItemInfo> items,
			int totalCount
	) {
	}

	@Builder
	public record PurchaseResult(
			Long remainingCoins,
			ItemInfo purchasedItem
	) {
	}

	@Builder
	public record UseResult(
			Long memberItemId,
			String itemName,
			Long remainingCount
	) {
	}
}
