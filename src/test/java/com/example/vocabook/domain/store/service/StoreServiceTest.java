package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.code.StoreErrorCode;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.domain.store.service.strategy.DefaultItemUseStrategy;
import com.example.vocabook.domain.store.service.strategy.ItemUseStrategy;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 단위 테스트")
public class StoreServiceTest {

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private MemberItemRepository memberItemRepository;
	@Mock
	private MemberRepository memberRepository;

	private StoreService storeService;

	private Member member;
	private Member otherMember;
	private AuthMember authMember;
	private Item item;
	private MemberItem memberItem;

	@BeforeEach
	void setUp() {
		List<ItemUseStrategy> strategies = List.of(new DefaultItemUseStrategy());
		storeService = new StoreService(itemRepository, memberItemRepository, memberRepository, strategies);

		member = Member.builder()
						 .id(1L)
						 .email("test@example.com")
						 .nickname("테스터")
						 .password("password")
						 .refreshToken("token")
						 .coin(200L)
						 .build();

		otherMember = Member.builder()
							  .id(2L)
							  .email("other@example.com")
							  .nickname("타인")
							  .password("password")
							  .refreshToken("token")
							  .coin(0L)
							  .build();

		authMember = new AuthMember(member);

		item = Item.builder()
					   .id(1L)
					   .name("연속학습 파괴 방어권")
					   .price(100L)
					   .itemType(ItemType.STREAK_FREEZE)
					   .build();

		memberItem = MemberItem.builder()
							 .id(10L)
							 .member(member)
							 .item(item)
							 .build();
	}

	@Test
	@DisplayName("상점 아이템 목록 조회 성공")
	void getItemList_Success() {
		given(itemRepository.findAll()).willReturn(List.of(item));

		StoreResDTO.ItemList result = storeService.getItemList();

		assertNotNull(result);
		assertEquals(1, result.totalCount());
		assertEquals("연속학습 파괴 방어권", result.items().get(0).name());
	}

	@Test
	@DisplayName("아이템 구매 성공 - 코인 차감 및 MemberItem 저장")
	void purchaseItem_Success() {
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.save(any())).willReturn(memberItem);

		StoreResDTO.PurchaseResult result = storeService.purchaseItem(1L, authMember);

		assertNotNull(result);
		assertEquals(100L, result.remainingCoins()); // 200 - 100
		assertEquals("연속학습 파괴 방어권", result.purchasedItem().name());
		verify(memberItemRepository).save(any(MemberItem.class));
	}

	@Test
	@DisplayName("아이템 구매 실패 - 존재하지 않는 아이템")
	void purchaseItem_ItemNotFound() {
		given(itemRepository.findById(99L)).willReturn(Optional.empty());

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.purchaseItem(99L, authMember));

		assertEquals(StoreErrorCode.ITEM_NOT_FOUND, ex.getCode());
	}

	@Test
	@DisplayName("아이템 구매 실패 - 코인 부족")
	void purchaseItem_InsufficientCoins() {
		Item expensiveItem = Item.builder()
									 .id(2L)
									 .name("고급 아이템")
									 .price(999L)
									 .itemType(ItemType.PET_FOOD_BASIC)
									 .build();

		given(itemRepository.findById(2L)).willReturn(Optional.of(expensiveItem));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.purchaseItem(2L, authMember));

		assertEquals(StoreErrorCode.INSUFFICIENT_COINS, ex.getCode());
	}

	@Test
	@DisplayName("보유 아이템 목록 조회 성공")
	void getMyItems_Success() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMember(member)).willReturn(List.of(memberItem));

		StoreResDTO.MyItemList result = storeService.getMyItems(authMember);

		assertNotNull(result);
		assertEquals(1, result.totalCount());
		assertEquals(10L, result.items().get(0).memberItemId());
	}

	@Test
	@DisplayName("아이템 사용 성공 - MemberItem 삭제 및 잔여 개수 반환")
	void useItem_Success() {
		given(memberItemRepository.findWithItemById(10L)).willReturn(Optional.of(memberItem));
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.countByMemberAndItem(member, item)).willReturn(0L);

		StoreResDTO.UseResult result = storeService.useItem(10L, authMember);

		assertNotNull(result);
		assertEquals(10L, result.memberItemId());
		assertEquals("연속학습 파괴 방어권", result.itemName());
		assertEquals(0L, result.remainingCount());
		verify(memberItemRepository).delete(memberItem);
	}

	@Test
	@DisplayName("아이템 사용 실패 - 존재하지 않는 MemberItem")
	void useItem_MemberItemNotFound() {
		given(memberItemRepository.findWithItemById(99L)).willReturn(Optional.empty());

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.useItem(99L, authMember));

		assertEquals(StoreErrorCode.ITEM_NOT_OWNED, ex.getCode());
	}

	@Test
	@DisplayName("아이템 사용 실패 - 다른 멤버의 아이템")
	void useItem_ItemNotOwned_WrongMember() {
		MemberItem othersMemberItem = MemberItem.builder()
											  .id(20L)
											  .member(otherMember)
											  .item(item)
											  .build();

		given(memberItemRepository.findWithItemById(20L)).willReturn(Optional.of(othersMemberItem));
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.useItem(20L, authMember));

		assertEquals(StoreErrorCode.ITEM_NOT_OWNED, ex.getCode());
	}

	@Disabled("비소모템 ItemType 추가 시 활성화")
	@Test
	@DisplayName("중복 구매 불가 아이템 - 이미 보유 중이면 예외")
	void purchaseItem_AlreadyOwned_NotAllowDuplicate() {
		// allowDuplicate=false인 ItemType 추가 후 아래 코드 완성
		// Item nonDuplicateItem = Item.builder()
		//         .id(3L).name("프로필 프레임").price(50L).itemType(ItemType.PROFILE_FRAME).build();
		// given(itemRepository.findById(3L)).willReturn(Optional.of(nonDuplicateItem));
		// given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		// given(memberItemRepository.findFirstByMemberAndItem(member, nonDuplicateItem))
		//         .willReturn(Optional.of(memberItem));
		// StoreException ex = assertThrows(StoreException.class,
		//         () -> storeService.purchaseItem(3L, authMember));
		// assertEquals(StoreErrorCode.ITEM_ALREADY_OWNED, ex.getCode());
	}

	@Test
	@DisplayName("중복 구매 가능 아이템 - 이미 보유 중이어도 구매 성공")
	void purchaseItem_AllowDuplicate_Success() {
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.save(any())).willReturn(memberItem);

		// STREAK_FREEZE는 allowDuplicate=true → 이미 보유 중 여부 체크 없이 구매 성공
		StoreResDTO.PurchaseResult result = storeService.purchaseItem(1L, authMember);

		assertNotNull(result);
		assertEquals(100L, result.remainingCoins());
	}
}
