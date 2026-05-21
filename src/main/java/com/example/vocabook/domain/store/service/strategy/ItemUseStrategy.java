package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.enums.ItemType;

public interface ItemUseStrategy {

	boolean supports(ItemType itemType);

	void apply(Member member, MemberItem memberItem);
}
