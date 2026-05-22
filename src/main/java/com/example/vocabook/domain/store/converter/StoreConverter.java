package com.example.vocabook.domain.store.converter;

import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;

import java.util.List;

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

	public static StoreResDTO.MyItemInfo toMyItemInfo(MemberItem memberItem) {
		return StoreResDTO.MyItemInfo.builder()
					   .memberItemId(memberItem.getId())
					   .item(toItemInfo(memberItem.getItem()))
					   .build();
	}

	public static StoreResDTO.MyItemList toMyItemList(List<MemberItem> memberItems) {
		return StoreResDTO.MyItemList.builder()
					   .items(memberItems.stream().map(StoreConverter::toMyItemInfo).toList())
					   .totalCount(memberItems.size())
					   .build();
	}

	public static StoreResDTO.PurchaseResult toPurchaseResult(long remainingCoins, Item item) {
		return StoreResDTO.PurchaseResult.builder()
					   .remainingCoins(remainingCoins)
					   .purchasedItem(toItemInfo(item))
					   .build();
	}

	public static StoreResDTO.UseResult toUseResult(Long memberItemId, MemberItem memberItem, long remainingCount, StoreResDTO.HintResult hintResult) {
		return StoreResDTO.UseResult.builder()
					   .memberItemId(memberItemId)
					   .itemName(memberItem.getItem().getName())
					   .remainingCount(remainingCount)
					   .hintResult(hintResult)
					   .build();
	}
}
