package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.code.StoreErrorCode;
import com.example.vocabook.domain.store.converter.StoreConverter;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.domain.store.service.strategy.ItemUseStrategy;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

	private final ItemRepository itemRepository;
	private final MemberItemRepository memberItemRepository;
	private final MemberRepository memberRepository;
	private final List<ItemUseStrategy> itemUseStrategies;

	public StoreResDTO.ItemList getItemList() {
		List<Item> items = itemRepository.findAll();
		return StoreConverter.toItemList(items);
	}

	@Transactional
	public StoreResDTO.PurchaseResult purchaseItem(Long itemId, AuthMember authMember) {
		Item item = itemRepository.findById(itemId)
							.orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

		Member member = memberRepository.findByIdWithLock(authMember.getMember().getId())
								.orElseThrow();

		if (!item.getItemType().isAllowDuplicate()) {
			boolean alreadyOwned = memberItemRepository.findFirstByMemberAndItem(member, item).isPresent();
			if (alreadyOwned) {
				throw new StoreException(StoreErrorCode.ITEM_ALREADY_OWNED);
			}
		}

		if (member.getCoin() < item.getPrice()) {
			throw new StoreException(StoreErrorCode.INSUFFICIENT_COINS);
		}

		member.spendCoin(item.getPrice());
		memberItemRepository.save(MemberItem.builder().member(member).item(item).build());

		return StoreConverter.toPurchaseResult(member.getCoin(), item);
	}

	public StoreResDTO.MyItemList getMyItems(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId())
								.orElseThrow();
		List<MemberItem> memberItems = memberItemRepository.findByMember(member);
		return StoreConverter.toMyItemList(memberItems);
	}

	@Transactional
	public StoreResDTO.UseResult useItem(Long memberItemId, AuthMember authMember) {
		MemberItem memberItem = memberItemRepository.findWithItemById(memberItemId)
										.orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_OWNED));

		Member member = memberRepository.findById(authMember.getMember().getId())
								.orElseThrow();

		if (!memberItem.getMember().getId().equals(member.getId())) {
			throw new StoreException(StoreErrorCode.ITEM_NOT_OWNED);
		}

		ItemUseStrategy strategy = itemUseStrategies.stream()
										   .filter(s -> s.supports(memberItem.getItem().getItemType()))
										   .findFirst()
										   .orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

		strategy.apply(member, memberItem);

		Item item = memberItem.getItem();
		memberItemRepository.delete(memberItem);

		long remaining = memberItemRepository.countByMemberAndItem(member, item);
		return StoreConverter.toUseResult(memberItemId, memberItem, remaining);
	}
}
