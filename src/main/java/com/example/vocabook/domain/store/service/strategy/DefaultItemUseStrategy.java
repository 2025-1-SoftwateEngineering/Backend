package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultItemUseStrategy implements ItemUseStrategy {

	@Override
	public boolean supports(ItemType itemType) {
		return true;
	}

	@Override
	public void apply(Member member, MemberItem memberItem) {
		// 소모 처리만 수행 (MemberItem 삭제는 StoreService에서 처리)
	}
}
