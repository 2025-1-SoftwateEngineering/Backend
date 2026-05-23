package com.example.vocabook.domain.pet.controller.docs;

import com.example.vocabook.domain.pet.dto.PetResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "펫 관련 API")
public interface PetControllerDocs {

	@Operation(
			summary = "내 펫 조회 API By 윤민재",
			description = """
					# 내 펫 조회

					## 요청 형식
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)

					## 응답
					- 펫의 레벨, 단계, 경험치, 배고픔/목마름 수치, 활성 배경, 보유 사료/물 개수 반환
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
									  "code": "PET200_1",
									  "message": "펫 정보를 성공적으로 조회했습니다.",
									  "result": {
									    "petId": 1,
									    "level": 3,
									    "stage": "BABY",
									    "currentXp": 120,
									    "hunger": 80,
									    "thirst": 40,
									    "activeBackground": "PET_BG_1",
									    "foodCount": 2,
									    "waterCount": 1
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
					description = "실패 - 펫이 없는 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "PET404_1",
									  "message": "펫을 찾을 수 없습니다.",
									  "result": null
									}
									""")
					)
			)
	})
	ApiResponse<PetResDTO.PetInfo> getPet(@AuthenticationPrincipal AuthMember authMember);

	@Operation(
			summary = "펫 생성 API By 윤민재",
			description = """
					# 펫 생성

					## 요청 형식
					- 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
					- 멤버당 1마리만 생성 가능

					## 응답
					- 생성된 펫 정보 반환 (레벨 1, 배고픔 50, 목마름 0)
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
									  "code": "PET200_2",
									  "message": "펫을 성공적으로 생성했습니다.",
									  "result": {
									    "petId": 1,
									    "level": 1,
									    "stage": "EGG",
									    "currentXp": 0,
									    "hunger": 50,
									    "thirst": 0,
									    "activeBackground": null,
									    "foodCount": 0,
									    "waterCount": 0
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
					description = "실패 - 이미 펫이 존재하는 경우",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "PET400_1",
									  "message": "이미 펫이 존재합니다.",
									  "result": null
									}
									""")
					)
			)
	})
	ApiResponse<PetResDTO.PetInfo> createPet(@AuthenticationPrincipal AuthMember authMember);
}
