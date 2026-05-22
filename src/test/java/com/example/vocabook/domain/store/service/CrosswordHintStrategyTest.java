package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberCrosswordRepository;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
import com.example.vocabook.domain.store.service.strategy.CrosswordHintStrategy;
import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.enums.ClueType;
import com.example.vocabook.domain.voca.repository.CrosswordHintRepository;
import com.example.vocabook.global.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrosswordHintStrategy 단위 테스트")
public class CrosswordHintStrategyTest {

	@Mock
	private CrosswordHintRepository crosswordHintRepository;
	@Mock
	private MemberCrosswordRepository memberCrosswordRepository;
	@Mock
	private RedisUtil redisUtil;

	private CrosswordHintStrategy strategy;

	private Member member;
	private Crossword crossword;
	private MemberCrossword memberCrossword;

	@BeforeEach
	void setUp() {
		strategy = new CrosswordHintStrategy(crosswordHintRepository, memberCrosswordRepository, redisUtil);
		member = Member.builder()
					   .id(1L)
					   .email("test@example.com")
					   .nickname("테스터")
					   .password("password")
					   .refreshToken("token")
					   .coin(500L)
					   .build();
		crossword = Crossword.builder().id(100L).build();
		memberCrossword = MemberCrossword.builder()
				.id(200L)
				.member(member)
				.crossword(crossword)
				.build();
	}

	private MemberItem buildMemberItem(ItemType itemType) {
		Item item = Item.builder()
					   .id(1L)
					   .name("힌트 아이템")
					   .price(100L)
					   .itemType(itemType)
					   .build();
		return MemberItem.builder()
					   .id(10L)
					   .member(member)
					   .item(item)
					   .build();
	}

	private CrosswordHint buildHint(String wordText, String startPoint, ClueType clueType) {
		Word word = Word.builder()
					   .id(50L)
					   .englishWord(wordText)
					   .meaning("의미")
					   .build();
		return CrosswordHint.builder()
				.id(1L)
				.word(word)
				.crossword(crossword)
				.wordStartPoint(startPoint)
				.clueType(clueType)
				.clueContent("단서")
				.build();
	}

	@Test
	@DisplayName("CROSSWORD_HINT_START - 첫 글자와 시작 좌표 반환")
	void apply_HintStart_ReturnsFirstLetter() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_START);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.ACROSS);

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.of(memberCrossword));
		given(redisUtil.hasKey("hint:1:200:1:START")).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, 1L);

		assertTrue(result.isPresent());
		assertEquals("A", result.get().letter());
		assertEquals(2, result.get().verticalStartPoint());   // 3-1
		assertEquals(4, result.get().horizontalStartPoint()); // 5-1
	}

	@Test
	@DisplayName("CROSSWORD_HINT_MIDDLE ACROSS - 중간 글자와 가로 방향 좌표 반환")
	void apply_HintMiddle_Across_ReturnsMiddleLetter() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_MIDDLE);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.ACROSS);
		// APPLE: len=5, len/2=2, charAt(2)='P', pos=(3-1, 5-1+2)=(2, 6)

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.of(memberCrossword));
		given(redisUtil.hasKey("hint:1:200:1:MIDDLE")).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, 1L);

		assertTrue(result.isPresent());
		assertEquals("P", result.get().letter());
		assertEquals(2, result.get().verticalStartPoint());
		assertEquals(6, result.get().horizontalStartPoint());
	}

	@Test
	@DisplayName("CROSSWORD_HINT_MIDDLE DOWN - 중간 글자와 세로 방향 좌표 반환")
	void apply_HintMiddle_Down_ReturnsMiddleLetter() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_MIDDLE);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.DOWN);
		// APPLE: len=5, len/2=2, charAt(2)='P', pos=(3-1+2, 5-1)=(4, 4)

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.of(memberCrossword));
		given(redisUtil.hasKey("hint:1:200:1:MIDDLE")).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, memberItem, 1L);

		assertTrue(result.isPresent());
		assertEquals("P", result.get().letter());
		assertEquals(4, result.get().verticalStartPoint());
		assertEquals(4, result.get().horizontalStartPoint());
	}

	@Test
	@DisplayName("contextId null이면 CROSSWORD_HINT_CONTEXT_REQUIRED 예외")
	void apply_NullContextId_Throws() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_START);

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, memberItem, null));
		assertEquals(StoreErrorCode.CROSSWORD_HINT_CONTEXT_REQUIRED, ex.getCode());
	}

	@Test
	@DisplayName("진행 중인 십자말풀이 없으면 CROSSWORD_NOT_IN_PROGRESS 예외")
	void apply_NoActiveGame_Throws() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_START);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.ACROSS);

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.empty());

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, memberItem, 1L));
		assertEquals(StoreErrorCode.CROSSWORD_NOT_IN_PROGRESS, ex.getCode());
	}

	@Test
	@DisplayName("같은 단어에 같은 힌트 중복 사용 차단 - HINT_ALREADY_USED 예외")
	void apply_DuplicateHint_Throws() {
		MemberItem memberItem = buildMemberItem(ItemType.CROSSWORD_HINT_START);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.ACROSS);

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.of(memberCrossword));
		given(redisUtil.hasKey("hint:1:200:1:START")).willReturn(true);

		StoreException ex = assertThrows(StoreException.class,
				() -> strategy.apply(member, memberItem, 1L));
		assertEquals(StoreErrorCode.HINT_ALREADY_USED, ex.getCode());
	}

	@Test
	@DisplayName("같은 단어에 START와 MIDDLE은 동시 허용 - 서로 다른 Redis 키")
	void apply_StartAndMiddle_AllowedSimultaneously() {
		MemberItem middleItem = buildMemberItem(ItemType.CROSSWORD_HINT_MIDDLE);
		CrosswordHint hint = buildHint("APPLE", "3 5", ClueType.ACROSS);

		given(crosswordHintRepository.findByIdWithCrossword(1L)).willReturn(Optional.of(hint));
		given(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
				.willReturn(Optional.of(memberCrossword));
		// START는 이미 사용됐지만 MIDDLE은 미사용
		given(redisUtil.hasKey("hint:1:200:1:MIDDLE")).willReturn(false);

		Optional<StoreResDTO.HintResult> result = strategy.apply(member, middleItem, 1L);

		assertTrue(result.isPresent());
		assertEquals("P", result.get().letter());
	}
}
