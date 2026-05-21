package com.example.vocabook.domain.store.controller.docs;

import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "상점 관련 API")
public interface StoreControllerDocs {

	@Operation(
			summary = "상점 아이템 목록 조회 API By 윤민재",
			description = """
					# 상점 아이템 목록 조회
					
					## 요청 형식
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					
					## 응답
					- 상점에 등록된 전체 아이템 목록 및 총 개수 반환
					"""
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_1",
									  "message": "상점 아이템 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "items": [
									      {
									        "itemId": 1,
									        "name": "연속학습 파괴 방어권",
									        "price": 100,
									        "itemType": "STREAK_FREEZE"
									      },
									      {
									        "itemId": 2,
									        "name": "사료 (기본)",
									        "price": 50,
									        "itemType": "PET_FOOD_BASIC"
									      }
									    ],
									    "totalCount": 2
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "401",
					description = "실패 - 로그인이 필요한 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "COMMON401_1",
									  "message": "인증이 필요합니다.",
									  "result": null
									}
									""")
					)
			)
	})
	ApiResponse<StoreResDTO.ItemList> getItemList();

	@Operation(
			summary = "아이템 구매 API By 윤민재",
			description = """
					# 아이템 구매
					
					## 요청 형식
					- itemId: 구매할 아이템 ID (PathVariable)
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					
					## 응답
					- 구매 후 남은 코인 및 구매한 아이템 정보 반환
					- 소모성 아이템은 중복 구매 가능
					- 비소모성 아이템(프로필/배경 등)은 중복 구매 불가
					"""
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_2",
									  "message": "아이템을 성공적으로 구매했습니다.",
									  "result": {
									    "remainingCoins": 900,
									    "purchasedItem": {
									      "itemId": 1,
									      "name": "연속학습 파괴 방어권",
									      "price": 100,
									      "itemType": "STREAK_FREEZE"
									    }
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "401",
					description = "실패 - 로그인이 필요한 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "COMMON401_1",
									  "message": "인증이 필요합니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 코인 부족",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_1",
									  "message": "코인이 부족합니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 이미 보유 중인 비소모 아이템",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_2",
									  "message": "이미 보유 중인 아이템입니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 존재하지 않는 아이템",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE404_1",
									  "message": "아이템을 찾을 수 없습니다.",
									  "result": null
									}
									""")
					)
			)
	})
	@Parameter(name = "itemId", description = "구매할 아이템 ID", required = true, example = "1")
	ApiResponse<StoreResDTO.PurchaseResult> purchaseItem(
			@PathVariable Long itemId,
			@AuthenticationPrincipal AuthMember authMember
	);

	@Operation(
			summary = "보유 아이템 목록 조회 API By 윤민재",
			description = """
					# 보유 아이템 목록 조회
					
					## 요청 형식
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					
					## 응답
					- 로그인한 멤버의 보유 아이템 전체 목록 반환
					- 동일 아이템 여러 개 보유 시 각각 개별 항목으로 반환 (memberItemId 다름)
					"""
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_3",
									  "message": "보유 아이템 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "items": [
									      {
									        "memberItemId": 10,
									        "item": {
									          "itemId": 1,
									          "name": "연속학습 파괴 방어권",
									          "price": 100,
									          "itemType": "STREAK_FREEZE"
									        }
									      },
									      {
									        "memberItemId": 11,
									        "item": {
									          "itemId": 1,
									          "name": "연속학습 파괴 방어권",
									          "price": 100,
									          "itemType": "STREAK_FREEZE"
									        }
									      }
									    ],
									    "totalCount": 2
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "401",
					description = "실패 - 로그인이 필요한 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "COMMON401_1",
									  "message": "인증이 필요합니다.",
									  "result": null
									}
									""")
					)
			)
	})
	ApiResponse<StoreResDTO.MyItemList> getMyItems(
			@AuthenticationPrincipal AuthMember authMember
	);

	@Operation(
			summary = "아이템 사용 API By 윤민재",
			description = """
					# 아이템 사용
					
					## 요청 형식
					- memberItemId: 사용할 보유 아이템 ID (PathVariable) — getMyItems에서 반환된 memberItemId 사용
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					
					## 응답
					- 사용한 아이템 이름 및 동일 아이템 잔여 개수 반환
					"""
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_4",
									  "message": "아이템을 성공적으로 사용했습니다.",
									  "result": {
									    "memberItemId": 10,
									    "itemName": "연속학습 파괴 방어권",
									    "remainingCount": 1
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "401",
					description = "실패 - 로그인이 필요한 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "COMMON401_1",
									  "message": "인증이 필요합니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 보유하지 않은 아이템",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE404_2",
									  "message": "보유하지 않은 아이템입니다.",
									  "result": null
									}
									""")
					)
			)
	})
	@Parameter(name = "memberItemId", description = "사용할 보유 아이템 ID (getMyItems에서 반환된 memberItemId)", required = true, example = "10")
	ApiResponse<StoreResDTO.UseResult> useItem(
			@PathVariable Long memberItemId,
			@AuthenticationPrincipal AuthMember authMember
	);
}
