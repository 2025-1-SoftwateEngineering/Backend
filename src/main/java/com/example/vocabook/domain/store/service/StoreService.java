package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberActiveProfile;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberActiveProfileRepository;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.pet.repository.MemberPetRepository;
import com.example.vocabook.domain.store.converter.StoreConverter;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.domain.store.service.strategy.ItemUseStrategy;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

	private final ItemRepository itemRepository;
	private final MemberItemRepository memberItemRepository;
	private final MemberRepository memberRepository;
	private final MemberPetRepository memberPetRepository;
	private final List<ItemUseStrategy> itemUseStrategies;
    private final MemberActiveProfileRepository memberActiveProfileRepository;

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

		if (member.getCoin() < item.getPrice()) {
			throw new StoreException(StoreErrorCode.INSUFFICIENT_COINS);
		}

		Optional<MemberItem> existing = memberItemRepository.findByMemberAndItem(member, item);

		if (!item.getItemType().isAllowDuplicate() && existing.isPresent()) {
			throw new StoreException(StoreErrorCode.ITEM_ALREADY_OWNED);
		}

		member.spendCoin(item.getPrice());

		existing.ifPresentOrElse(
				MemberItem::increaseCount,
				() -> memberItemRepository.save(MemberItem.builder().member(member).item(item).build())
		);

		return StoreConverter.toPurchaseResult(member.getCoin(), item);
	}

	public StoreResDTO.MyItemList getMyItems(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId())
								.orElseThrow();
		List<MemberItem> memberItems = memberItemRepository.findByMember(member);

		Set<Long> equippedItemIds = new java.util.HashSet<>();
		memberPetRepository.findByMember(member).ifPresent(pet -> {
			if (pet.getActiveBackground() != null) equippedItemIds.add(pet.getActiveBackground().getId());
			if (pet.getActiveAccessory() != null) equippedItemIds.add(pet.getActiveAccessory().getId());
		});

		return StoreConverter.toMyItemList(memberItems, equippedItemIds);
	}

	@Transactional
	public StoreResDTO.UseResult useItem(Long itemId, AuthMember authMember, Long contextId) {
		Member member = memberRepository.findById(authMember.getMember().getId())
								.orElseThrow();

		Item item = itemRepository.findById(itemId)
							.orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

		MemberItem memberItem = memberItemRepository.findByMemberAndItemWithLock(member, item)
										.orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_OWNED));

		ItemUseStrategy strategy = itemUseStrategies.stream()
										   .filter(s -> s.supports(item.getItemType()))
										   .findFirst()
										   .orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

		long remaining;
		if (item.getItemType().isAllowDuplicate()) {
			if (memberItem.getCount() <= 1) {
				memberItemRepository.delete(memberItem);
				remaining = 0;
			} else {
				remaining = memberItem.decreaseCount();
			}
		} else {
			remaining = memberItem.getCount();
		}

		Optional<StoreResDTO.HintResult> hint = strategy.apply(member, memberItem, contextId);
		return StoreConverter.toUseResult(item, remaining, hint.orElse(null));
	}
}
