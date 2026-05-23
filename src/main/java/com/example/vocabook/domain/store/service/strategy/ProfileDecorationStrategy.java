package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class ProfileDecorationStrategy implements ItemUseStrategy {

	@Override
	public boolean supports(ItemType itemType) {
		return itemType == ItemType.PROFILE_PHOTO_1
				|| itemType == ItemType.PROFILE_PHOTO_2
				|| itemType == ItemType.PROFILE_BG_1
				|| itemType == ItemType.PROFILE_BG_2;
	}

	@Override
	public Optional<StoreResDTO.HintResult> apply(Member member, MemberItem memberItem, Long contextId) {
		member.applyProfileDecoration(memberItem.getItem().getItemType());
		return Optional.empty();
	}
}
