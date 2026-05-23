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
			ItemInfo item,
			Long count,
			boolean isEquipped
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
			String itemName,
			Long remainingCount,
			HintResult hintResult
	) {
	}

	@Builder
	public record HintResult(
			String letter,
			int verticalStartPoint,
			int horizontalStartPoint
	) {
	}
}
