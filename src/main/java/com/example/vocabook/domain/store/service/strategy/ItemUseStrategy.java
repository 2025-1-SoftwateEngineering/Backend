package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.enums.ItemType;

import java.util.Optional;

public interface ItemUseStrategy {

	boolean supports(ItemType itemType);

	Optional<StoreResDTO.HintResult> apply(Member member, MemberItem memberItem, Long contextId);
}
