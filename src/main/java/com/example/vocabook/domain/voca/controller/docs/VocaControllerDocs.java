package com.example.vocabook.domain.voca.controller.docs;

import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "단어 관련 API")
public interface VocaControllerDocs {

	@Operation(
			summary = "단어장 전체 목록 조회 API By 윤민재",
			description = """
					# 단어장 전체 목록 조회

					## 요청 형식
					- 인증 불필요

					## 응답
					- 전체 단어장 목록, 단어장별 단어 수 반환
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
									  "code": "VOCA200_5",
									  "message": "단어장 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "vocas": [
									      {
									        "vocaId": 1,
									        "description": "TOEIC 핵심 동사",
									        "wordCount": 20,
									        "createdAt": "2026-01-10T09:00:00"
									      }
									    ],
									    "totalCount": 1
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.VocaList> getVocaList(
			@AuthenticationPrincipal AuthMember authMember
	);

	@Operation(
			summary = "단어 암기 저장 API By 윤민재",
			description = """
					# 단어 암기 저장

					## 요청 형식
					- vocaId: 단어장 ID
					- wordIds: 암기한 단어 ID 목록

					## 응답
					- 해당 단어장에서 암기한 전체 단어 ID 목록 반환
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
									  "code": "VOCA200_7",
									  "message": "암기한 단어를 성공적으로 저장했습니다.",
									  "result": {
									    "memorizedWordIds": [1, 2, 3],
									    "totalCount": 3
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.MemorizeInfo> memorizeWords(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.Memorize dto
	);

	@Operation(
			summary = "암기한 단어 목록 조회 API By 윤민재",
			description = """
					# 암기한 단어 목록 조회

					## 요청 형식
					- vocaId: 단어장 ID

					## 응답
					- 해당 단어장에서 암기한 단어 ID 목록 반환
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
									  "code": "VOCA200_8",
									  "message": "암기한 단어 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "memorizedWordIds": [1, 2, 3],
									    "totalCount": 3
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.MemorizeInfo> getMemorizedWords(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember
	);

	@Operation(
			summary = "학습한 단어장 목록 조회 API By 윤민재",
			description = """
					# 학습한 단어장 목록 조회

					## 요청 형식
					- 인증 토큰 필요 (JWT)

					## 응답
					- 학습한 단어장 목록 및 총 개수 반환
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
									  "code": "VOCA200_4",
									  "message": "학습한 단어장 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "vocas": [
									      {
									        "vocaId": 1,
									        "description": "기초 영단어",
									        "learningWordCnt": 20,
									        "correctCnt": 15,
									        "solvedAt": "2026-05-10T12:00:00"
									      }
									    ],
									    "totalCount": 1
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.StudiedVocaList> getStudiedVocas(
			@AuthenticationPrincipal AuthMember authMember
	);

	@Operation(
			summary = "단어 목록 조회 API By 윤민재",
			description = """
					# 단어 목록 조회

					## 요청 형식
					- vocaId: 단어장 ID
					- page: 페이지 번호 (0부터 시작, 기본값 0)
					- pageSize: 페이지당 단어 수 (기본값 10)
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
									  "code": "VOCA200_1",
									  "message": "단어 목록을 성공적으로 불러왔습니다.",
									  "result": {
									    "vocaId": 1,
									    "description": "TOEIC 핵심 동사",
									    "words": [
									      { "wordId": 1, "englishWord": "apple", "meaning": "사과" },
									      { "wordId": 2, "englishWord": "banana", "meaning": "바나나" }
									    ],
									    "totalPages": 5,
									    "totalElements": 50
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.WordList> getWords(
			@PathVariable Long vocaId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int pageSize
	);

	@Operation(
			summary = "단어 테스트 문제 조회 API By 윤민재",
			description = """
					# 단어 테스트 문제 조회

					## 요청 형식
					- vocaId: 단어장 ID
					- 뜻만 반환하며 랜덤 순서로 제공
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
									  "code": "VOCA200_2",
									  "message": "테스트 문제를 성공적으로 불러왔습니다.",
									  "result": [
									    { "wordId": 3, "meaning": "바나나" },
									    { "wordId": 1, "meaning": "사과" }
									  ]
									}
									""")
					)
			)
	})
	ApiResponse<List<VocaResDTO.TestQuestion>> getTestQuestions(
			@PathVariable Long vocaId
	);

	@Operation(
			summary = "단어 테스트 결과 제출 API By 윤민재",
			description = """
					# 단어 테스트 결과 제출

					## 요청 형식
					- vocaId: 단어장 ID
					- answers: 단어별 답안 목록 (wordId + answer)
					- 대소문자 구분 없이 채점

					## 응답
					- 정답 개수, 획득 재화, 단어별 채점 결과 반환
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
									  "code": "VOCA200_3",
									  "message": "테스트 결과를 성공적으로 제출했습니다.",
									  "result": {
									    "totalCount": 2,
									    "correctCount": 1,
									    "earnedCoins": 5,
									    "results": [
									      {
									        "wordId": 1,
									        "meaning": "사과",
									        "correctAnswer": "apple",
									        "submittedAnswer": "apple",
									        "isCorrect": true
									      }
									    ]
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.TestResult> submitTest(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.SubmitTest dto
	);
}
