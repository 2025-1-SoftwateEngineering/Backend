package com.example.vocabook.domain.store.controller;

import com.example.vocabook.domain.store.code.StoreSuccessCode;
import com.example.vocabook.domain.store.controller.docs.StoreControllerDocs;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.service.StoreService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/store")
public class StoreController implements StoreControllerDocs {

	private final StoreService storeService;

	@GetMapping("/items")
	public ApiResponse<StoreResDTO.ItemList> getItemList() {
		return ApiResponse.onSuccess(StoreSuccessCode.GET_ITEM_LIST, storeService.getItemList());
	}

	@PostMapping("/items/{itemId}/purchase")
	public ApiResponse<StoreResDTO.PurchaseResult> purchaseItem(
			@PathVariable Long itemId,
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(StoreSuccessCode.PURCHASE_ITEM, storeService.purchaseItem(itemId, authMember));
	}

	@GetMapping("/my-items")
	public ApiResponse<StoreResDTO.MyItemList> getMyItems(
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(StoreSuccessCode.GET_MY_ITEMS, storeService.getMyItems(authMember));
	}

	@PostMapping("/my-items/{memberItemId}/use")
	public ApiResponse<StoreResDTO.UseResult> useItem(
			@PathVariable Long memberItemId,
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(StoreSuccessCode.USE_ITEM, storeService.useItem(memberItemId, authMember));
	}
}
