package com.example.vocabook.domain.store.converter;

import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;

import java.util.List;
import java.util.Set;

public class StoreConverter {

	public static StoreResDTO.ItemInfo toItemInfo(Item item) {
		return StoreResDTO.ItemInfo.builder()
					   .itemId(item.getId())
					   .name(item.getName())
					   .price(item.getPrice())
					   .itemType(item.getItemType())
					   .build();
	}

	public static StoreResDTO.ItemList toItemList(List<Item> items) {
		return StoreResDTO.ItemList.builder()
					   .items(items.stream().map(StoreConverter::toItemInfo).toList())
					   .totalCount(items.size())
					   .build();
	}

	public static StoreResDTO.MyItemInfo toMyItemInfo(MemberItem memberItem, Set<ItemType> equippedTypes) {
		return StoreResDTO.MyItemInfo.builder()
					   .item(toItemInfo(memberItem.getItem()))
					   .count(memberItem.getCount())
					   .isEquipped(equippedTypes.contains(memberItem.getItem().getItemType()))
					   .build();
	}

	public static StoreResDTO.MyItemList toMyItemList(List<MemberItem> memberItems, Set<ItemType> equippedTypes) {
		return StoreResDTO.MyItemList.builder()
					   .items(memberItems.stream().map(mi -> toMyItemInfo(mi, equippedTypes)).toList())
					   .totalCount(memberItems.size())
					   .build();
	}

	public static StoreResDTO.PurchaseResult toPurchaseResult(long remainingCoins, Item item) {
		return StoreResDTO.PurchaseResult.builder()
					   .remainingCoins(remainingCoins)
					   .purchasedItem(toItemInfo(item))
					   .build();
	}

	public static StoreResDTO.UseResult toUseResult(Item item, long remainingCount, StoreResDTO.HintResult hintResult) {
		return StoreResDTO.UseResult.builder()
					   .itemName(item.getName())
					   .remainingCount(remainingCount)
					   .hintResult(hintResult)
					   .build();
	}
}
