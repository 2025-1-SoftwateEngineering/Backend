package com.example.vocabook.domain.store.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.member.repository.MemberActiveProfileRepository;
import com.example.vocabook.domain.member.repository.MemberItemRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.exception.code.StoreErrorCode;
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
	@Mock
	private com.example.vocabook.domain.pet.repository.MemberPetRepository memberPetRepository;
	@Mock
	private MemberActiveProfileRepository memberActiveProfileRepository;

	private StoreService storeService;

	private Member member;
	private AuthMember authMember;
	private Item item;
	private MemberItem memberItem;

	@BeforeEach
	void setUp() {
		List<ItemUseStrategy> strategies = List.of(new DefaultItemUseStrategy());
		storeService = new StoreService(itemRepository, memberItemRepository, memberRepository, memberPetRepository, strategies, memberActiveProfileRepository);

		member = Member.builder()
						 .id(1L)
						 .email("test@example.com")
						 .nickname("테스터")
						 .password("password")
						 .refreshToken("token")
						 .coin(200L)
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
							 .count(1L)
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
	@DisplayName("아이템 구매 성공 - 첫 구매 시 새 row 생성")
	void purchaseItem_Success_FirstPurchase() {
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMemberAndItem(member, item)).willReturn(Optional.empty());
		given(memberItemRepository.save(any())).willReturn(memberItem);

		StoreResDTO.PurchaseResult result = storeService.purchaseItem(1L, authMember);

		assertNotNull(result);
		assertEquals(100L, result.remainingCoins()); // 200 - 100
		assertEquals("연속학습 파괴 방어권", result.purchasedItem().name());
		verify(memberItemRepository).save(any(MemberItem.class));
	}

	@Test
	@DisplayName("아이템 구매 성공 - 중복 구매 시 count 증가")
	void purchaseItem_Success_DuplicatePurchase() {
		MemberItem existingItem = MemberItem.builder()
				.id(10L).member(member).item(item).count(2L).build();

		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMemberAndItem(member, item)).willReturn(Optional.of(existingItem));

		storeService.purchaseItem(1L, authMember);

		assertEquals(3L, existingItem.getCount());
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
									 .itemType(ItemType.PET_FOOD)
									 .build();

		given(itemRepository.findById(2L)).willReturn(Optional.of(expensiveItem));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.purchaseItem(2L, authMember));

		assertEquals(StoreErrorCode.INSUFFICIENT_COINS, ex.getCode());
	}

	@Test
	@DisplayName("보유 아이템 목록 조회 성공 - 소모성 아이템은 isEquipped=false")
	void getMyItems_Success_ConsumableIsNotEquipped() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMember(member)).willReturn(List.of(memberItem));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.empty());

		StoreResDTO.MyItemList result = storeService.getMyItems(authMember);

		assertNotNull(result);
		assertEquals(1, result.totalCount());
		assertEquals("연속학습 파괴 방어권", result.items().get(0).item().name());
		assertFalse(result.items().get(0).isEquipped());
	}

	@Test
	@DisplayName("보유 아이템 목록 조회 - 장착 중인 치장 아이템은 isEquipped=true")
	void getMyItems_DecorationItem_IsEquipped() {
		Item bgItem = Item.builder()
				.id(3L).name("펫 배경 1").price(200L).itemType(ItemType.PET_BG).build();
		MemberItem bgMemberItem = MemberItem.builder()
				.id(20L).member(member).item(bgItem).count(1L).build();

		com.example.vocabook.domain.member.entity.mapping.MemberPet pet =
				com.example.vocabook.domain.member.entity.mapping.MemberPet.builder()
						.id(1L).member(member).build();
		pet.changeBackground(bgItem);

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMember(member)).willReturn(List.of(bgMemberItem));
		given(memberPetRepository.findByMember(member)).willReturn(Optional.of(pet));

		StoreResDTO.MyItemList result = storeService.getMyItems(authMember);

		assertTrue(result.items().get(0).isEquipped());
	}

	@Test
	@DisplayName("아이템 사용 성공 - count가 1이면 row 삭제")
	void useItem_Success_DeleteWhenLastItem() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberItemRepository.findByMemberAndItemWithLock(member, item)).willReturn(Optional.of(memberItem));

		StoreResDTO.UseResult result = storeService.useItem(1L, authMember, null);

		assertNotNull(result);
		assertEquals("연속학습 파괴 방어권", result.itemName());
		assertEquals(0L, result.remainingCount());
		assertNull(result.hintResult());
		verify(memberItemRepository).delete(memberItem);
	}

	@Test
	@DisplayName("아이템 사용 성공 - count > 1이면 count 차감")
	void useItem_Success_DecrementCount() {
		MemberItem multiItem = MemberItem.builder()
				.id(10L).member(member).item(item).count(3L).build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberItemRepository.findByMemberAndItemWithLock(member, item)).willReturn(Optional.of(multiItem));

		StoreResDTO.UseResult result = storeService.useItem(1L, authMember, null);

		assertEquals(2L, result.remainingCount());
		assertEquals(2L, multiItem.getCount());
	}

	@Test
	@DisplayName("아이템 사용 실패 - 보유하지 않은 아이템")
	void useItem_ItemNotOwned() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(itemRepository.findById(1L)).willReturn(Optional.of(item));
		given(memberItemRepository.findByMemberAndItemWithLock(member, item)).willReturn(Optional.empty());

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.useItem(1L, authMember, null));

		assertEquals(StoreErrorCode.ITEM_NOT_OWNED, ex.getCode());
	}

	@Test
	@DisplayName("중복 구매 불가 아이템 - 이미 보유 중이면 예외")
	void purchaseItem_AlreadyOwned_NotAllowDuplicate() {
		Item bgItem = Item.builder()
				.id(3L)
				.name("펫 배경 1")
				.price(100L)
				.itemType(ItemType.PET_BG)
				.build();
		MemberItem existing = MemberItem.builder()
				.id(20L).member(member).item(bgItem).count(1L).build();

		given(itemRepository.findById(3L)).willReturn(Optional.of(bgItem));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMemberAndItem(member, bgItem)).willReturn(Optional.of(existing));

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.purchaseItem(3L, authMember));

		assertEquals(StoreErrorCode.ITEM_ALREADY_OWNED, ex.getCode());
	}

	@Test
	@DisplayName("아이템 구매 성공 - 코인이 정확히 아이템 가격과 같을 때")
	void purchaseItem_Success_ExactCoins() {
		Item exactPriceItem = Item.builder()
				.id(4L)
				.name("딱 맞는 아이템")
				.price(200L)
				.itemType(ItemType.PET_FOOD)
				.build();

		given(itemRepository.findById(4L)).willReturn(Optional.of(exactPriceItem));
		given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(member));
		given(memberItemRepository.findByMemberAndItem(member, exactPriceItem)).willReturn(Optional.empty());
		given(memberItemRepository.save(any())).willReturn(
				MemberItem.builder().id(11L).member(member).item(exactPriceItem).count(1L).build());

		StoreResDTO.PurchaseResult result = storeService.purchaseItem(4L, authMember);

		assertEquals(0L, result.remainingCoins());
	}

	@Test
	@DisplayName("아이템 사용 실패 - 존재하지 않는 아이템 ID")
	void useItem_ItemNotFound() {
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(itemRepository.findById(99L)).willReturn(Optional.empty());

		StoreException ex = assertThrows(StoreException.class,
				() -> storeService.useItem(99L, authMember, null));

		assertEquals(StoreErrorCode.ITEM_NOT_FOUND, ex.getCode());
	}

	@Test
	@DisplayName("비소모성 아이템 사용 시 count가 유지됨 (삭제되지 않음)")
	void useItem_NonConsumable_CountNotDecreased() {
		Item bgItem = Item.builder()
				.id(3L)
				.name("펫 배경 1")
				.price(200L)
				.itemType(ItemType.PET_BG)
				.build();
		MemberItem bgMemberItem = MemberItem.builder()
				.id(20L).member(member).item(bgItem).count(1L).build();

		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(itemRepository.findById(3L)).willReturn(Optional.of(bgItem));
		given(memberItemRepository.findByMemberAndItemWithLock(member, bgItem)).willReturn(Optional.of(bgMemberItem));

		StoreResDTO.UseResult result = storeService.useItem(3L, authMember, null);

		assertEquals(1L, result.remainingCount());
		assertEquals(1L, bgMemberItem.getCount());
	}
}
