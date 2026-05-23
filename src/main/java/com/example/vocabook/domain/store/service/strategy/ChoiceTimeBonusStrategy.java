package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class ChoiceTimeBonusStrategy implements ItemUseStrategy {

	private final RedisUtil redisUtil;

	@Override
	public boolean supports(ItemType itemType) {
		return itemType == ItemType.CHOICE_TIME_10 || itemType == ItemType.CHOICE_TIME_30;
	}

	@Override
	public Optional<StoreResDTO.HintResult> apply(Member member, MemberItem memberItem, Long contextId) {
		ItemType itemType = memberItem.getItem().getItemType();
		int bonus = itemType == ItemType.CHOICE_TIME_10 ? 10 : 30;
		String redisKey = "bonus:choiceTime:" + bonus + ":" + member.getId();

		if (redisUtil.hasKey(redisKey)) {
			throw new StoreException(StoreErrorCode.CHOICE_TIME_ALREADY_ACTIVE);
		}

		redisUtil.save(redisKey, bonus, Duration.ofHours(1));
		return Optional.empty();
	}
}
