package com.example.vocabook.domain.store.service.strategy;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberCrosswordRepository;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.enums.ClueType;
import com.example.vocabook.domain.voca.repository.CrosswordHintRepository;
import com.example.vocabook.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class CrosswordHintStrategy implements ItemUseStrategy {

	private final CrosswordHintRepository crosswordHintRepository;
	private final MemberCrosswordRepository memberCrosswordRepository;
	private final RedisUtil redisUtil;

	@Override
	public boolean supports(ItemType itemType) {
		return itemType == ItemType.CROSSWORD_HINT_START || itemType == ItemType.CROSSWORD_HINT_MIDDLE;
	}

	@Override
	public Optional<StoreResDTO.HintResult> apply(Member member, MemberItem memberItem, Long contextId) {
		if (contextId == null) {
			throw new StoreException(StoreErrorCode.CROSSWORD_HINT_CONTEXT_REQUIRED);
		}

		CrosswordHint crosswordHint = crosswordHintRepository.findByIdWithCrossword(contextId)
				.orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

		MemberCrossword memberCrossword = memberCrosswordRepository
				.findByMemberAndCrosswordAndSolvedAtIsNull(member, crosswordHint.getCrossword())
				.orElseThrow(() -> new StoreException(StoreErrorCode.CROSSWORD_NOT_IN_PROGRESS));

		ItemType itemType = memberItem.getItem().getItemType();
		String hintTypeKey = itemType == ItemType.CROSSWORD_HINT_START ? "START" : "MIDDLE";
		String redisKey = "hint:" + member.getId() + ":" + memberCrossword.getId() + ":" + contextId + ":" + hintTypeKey;

		if (redisUtil.hasKey(redisKey)) {
			throw new StoreException(StoreErrorCode.HINT_ALREADY_USED);
		}

		String word = crosswordHint.getWord().getEnglishWord();
		int len = word.length();
		String[] parts = crosswordHint.getWordStartPoint().split(" ");
		int startRow = Integer.parseInt(parts[0]);
		int startCol = Integer.parseInt(parts[1]);

		char letter;
		int verticalPos;
		int horizontalPos;

		if (itemType == ItemType.CROSSWORD_HINT_START) {
			letter = word.charAt(0);
			verticalPos = startRow - 1;
			horizontalPos = startCol - 1;
		} else {
			letter = word.charAt(len / 2);
			if (crosswordHint.getClueType() == ClueType.ACROSS) {
				verticalPos = startRow - 1;
				horizontalPos = startCol - 1 + len / 2;
			} else {
				verticalPos = startRow - 1 + len / 2;
				horizontalPos = startCol - 1;
			}
		}

		redisUtil.save(redisKey, true, Duration.ofDays(2));

		return Optional.of(StoreResDTO.HintResult.builder()
				.letter(String.valueOf(letter))
				.verticalStartPoint(verticalPos)
				.horizontalStartPoint(horizontalPos)
				.build());
	}
}
