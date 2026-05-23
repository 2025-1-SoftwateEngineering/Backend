package com.example.vocabook.domain.store.controller.docs;

import com.example.vocabook.domain.store.dto.StoreReqDTO;
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
import org.springframework.web.bind.annotation.RequestBody;

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
					- 소모성 아이템은 중복 구매 가능 (count 증가)
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
					- 로그인한 멤버의 보유 아이템 목록 반환
					- 동일 아이템 여러 개 보유 시 count 필드로 개수 표시
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
									        "item": {
									          "itemId": 1,
									          "name": "연속학습 파괴 방어권",
									          "price": 100,
									          "itemType": "STREAK_FREEZE"
									        },
									        "count": 2
									      },
									      {
									        "item": {
									          "itemId": 2,
									          "name": "사료",
									          "price": 100,
									          "itemType": "PET_FOOD_BASIC"
									        },
									        "count": 1
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
					- itemId: 사용할 아이템 ID (PathVariable) — getItemList 또는 getMyItems에서 확인
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					- CROSSWORD_HINT_START, CROSSWORD_HINT_MIDDLE 아이템 사용 시 RequestBody에 contextId(CrosswordHint ID) 필수
					- CHOICE_TIME_10, CHOICE_TIME_30 아이템: 같은 종류 중복 사용 불가 (10+10, 30+30 차단, 10+30 허용)
					- CROSSWORD_HINT: 같은 단어에 같은 힌트 종류 중복 사용 불가 (START+START 차단, START+MIDDLE 허용)

					## 응답
					- 사용한 아이템 이름 및 동일 아이템 잔여 개수 반환
					- 십자말풀이 힌트 아이템의 경우 hintResult(letter, verticalStartPoint, horizontalStartPoint) 포함
					- 그 외 아이템의 경우 hintResult: null
					- verticalStartPoint, horizontalStartPoint는 **0-based index** (0이 첫 번째 칸, getCrossword 좌표계와 동일)
					"""
	)
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시 (일반 아이템)",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_4",
									  "message": "아이템을 성공적으로 사용했습니다.",
									  "result": {
									    "itemName": "연속학습 파괴 방어권",
									    "remainingCount": 1,
									    "hintResult": null
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "200",
					description = "성공 예시 (십자말풀이 힌트 아이템)",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "STORE200_4",
									  "message": "아이템을 성공적으로 사용했습니다.",
									  "result": {
									    "itemName": "첫 스펠링 힌트",
									    "remainingCount": 0,
									    "hintResult": {
									      "letter": "A",
									      "verticalStartPoint": 2,
									      "horizontalStartPoint": 3
									    }
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 힌트 아이템 contextId 누락",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_3",
									  "message": "힌트 아이템 사용 시 contextId가 필요합니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 진행 중인 십자말풀이 없음",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_4",
									  "message": "진행 중인 십자말풀이가 없습니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 동일 시간 보너스 아이템 중복 사용",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_5",
									  "message": "동일한 시간 보너스 아이템이 이미 활성화 중입니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "400",
					description = "실패 - 같은 단어에 같은 힌트 중복 사용",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "STORE400_6",
									  "message": "해당 단어에 같은 힌트를 이미 사용했습니다.",
									  "result": null
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
	@Parameter(name = "itemId", description = "사용할 아이템 ID (getItemList 또는 getMyItems에서 확인)", required = true, example = "1")
	ApiResponse<StoreResDTO.UseResult> useItem(
			@PathVariable Long itemId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody(required = false) StoreReqDTO.UseItemRequest request
	);
}
