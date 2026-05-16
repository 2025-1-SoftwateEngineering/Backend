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
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import org.springframework.http.ResponseEntity;
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
			summary = "단어 하나 즉시 채점 API By 윤민재",
			description = """
					# 단어 하나 즉시 채점

					## 요청 형식
					- vocaId: 단어장 ID
					- wordId: 단어 ID
					- answer: 제출 답안
					- 대소문자 구분 없이 채점, 인증 불필요

					## 응답
					- 해당 단어의 채점 결과 즉시 반환
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
									  "message": "채점 결과를 성공적으로 반환했습니다.",
									  "result": {
									    "wordId": 1,
									    "meaning": "사과",
									    "correctAnswer": "apple",
									    "submittedAnswer": "apple",
									    "isCorrect": true
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.AnswerResult> submitAnswer(
			@PathVariable Long vocaId,
			@RequestBody @Valid VocaReqDTO.SubmitAnswer dto
	);

	@Operation(
			summary = "테스트 완료 API By 윤민재",
			description = """
					# 테스트 완료

					## 요청 형식
					- vocaId: 단어장 ID
					- answers: 전체 답안 목록 (wordId + answer)
					- 인증 토큰 필요 (JWT)

					## 응답
					- 최종 결과, 획득 코인, 스트릭 반영
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
									  "code": "VOCA200_9",
									  "message": "테스트를 성공적으로 완료했습니다.",
									  "result": {
									    "totalCount": 2,
									    "correctCount": 1,
									    "wrongCount": 1,
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
	ApiResponse<VocaResDTO.TestResult> completeTest(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.CompleteTest dto
	);

	@Operation(
			summary = "사지선다 문제 목록 조회 API By 김주헌",
			description = """
					# 사지선다 문제 목록 조회

					## 요청 형식
					- cursor: 커서 값 (초기 요청 시 -1, 기본값 -1)
					- pageSize: 페이지당 조회 수 (기본값 10)
					- 인증 토큰 필요 (JWT)

					## 응답
					- 커서 기반 사지선다 문제 세트 목록 (사용자 플레이 횟수 포함) 반환
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
									  "message": "성공적으로 사지선다 문제 목록을 조회했습니다.",
									  "result": {
									    "data": [
									      {
									        "id": 1,
									        "solvedCoin": 100,
									        "cnt": 3
									      }
									    ],
									    "nextCursor": "1",
									    "hasNext": false,
									    "totalElements": 1
									  }
									}
									""")
					)
			)
	})
	ApiResponse<PagingResDTO.Cursor<VocaResDTO.GetChoiceList>> getChoiceList(
			@AuthenticationPrincipal AuthMember auth,
			@RequestParam(defaultValue = "-1") String cursor,
			@RequestParam(defaultValue = "10") Integer pageSize
	);

	@Operation(
			summary = "사지선다 문제 선택지 조회 API By 김주헌",
			description = """
					# 사지선다 문제 선택지 조회

					## 요청 형식
					- choiceId: 사지선다 문제 세트 ID
					- current: 현재 문제 ID (초기 요청 시 0)
					- 인증 토큰 필요 (JWT)

					## 응답
					- 특정 문제에 대한 선택지 정보 반환. 마지막 문제 이후 조회 시 204 No Content 응답
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
									  "message": "성공적으로 사지선다 선택지를 조회했습니다.",
									  "result": {
									    "id": 10,
									    "score": 0,
									    "question": "사과",
									    "choices": [
									      { "id": 1, "text": "apple" },
									      { "id": 2, "text": "banana" },
									      { "id": 3, "text": "grape" },
									      { "id": 4, "text": "orange" }
									    ]
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "204",
					description = "마지막 문제 조회 시 예시 (No Content)",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": "VOCA204_1",
									  "message": "마지막 문제였습니다. 결과를 조회해주세요.",
									  "result": null
									}
									""")
					)
			)
	})
	ResponseEntity<ApiResponse<VocaResDTO.GetChoice>> getChoice(
			@PathVariable Long choiceId,
			@RequestParam Long current,
			@AuthenticationPrincipal AuthMember auth
	);

	@Operation(
			summary = "사지선다 정답 제출 API By 김주헌",
			description = """
					# 사지선다 정답 제출

					## 요청 형식
					- choiceId: 사지선다 문제 세트 ID
					- answer: 제출한 단어 ID (타임아웃 시 0)
					- current: 현재 문제 ID
					- 인증 토큰 필요 (JWT)

					## 응답
					- 정답 여부, 다음 문제 존재 여부, 다음 문제 ID 및 누적 스코어 반환
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
									  "code": "VOCA200_6",
									  "message": "성공적으로 사지선다 정답을 제출했습니다.",
									  "result": {
									    "isCorrect": true,
									    "hasNext": true,
									    "nextCurrent": 2,
									    "score": 1000
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.SubmitChoice> submitChoice(
			@AuthenticationPrincipal AuthMember auth,
			@PathVariable Long choiceId,
			@RequestParam Long answer,
			@RequestParam Long current
	);

	@Operation(
			summary = "십자말풀이 목록 조회 API By 김주헌",
			description = """
					# 십자말풀이 목록 조회

					## 요청 형식
					- cursor: 커서 값 (초기 요청 시 -1, 기본값 -1)
					- pageSize: 페이지당 조회 수 (기본값 10)
					- 인증 토큰 필요 (JWT)

					## 응답
					- 커서 기반 십자말풀이 문제 세트 목록 (사용자 플레이 횟수 포함) 반환
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
									  "code": "VOCA200_10",
									  "message": "성공적으로 십자말풀이 목록을 조회했습니다.",
									  "result": {
									    "data": [
									      {
									        "id": 1,
									        "solvedCoin": 200,
									        "cnt": 5
									      }
									    ],
									    "nextCursor": "1",
									    "hasNext": false,
									    "totalElements": 1
									  }
									}
									""")
					)
			)
	})
	ApiResponse<PagingResDTO.Cursor<VocaResDTO.GetCrosswordList>> getCrosswordList(
			@AuthenticationPrincipal AuthMember auth,
			@RequestParam(defaultValue = "-1") String cursor,
			@RequestParam(defaultValue = "10") Integer pageSize
	);

	@Operation(
			summary = "십자말풀이 문제 조회 API By 김주헌",
			description = """
					# 십자말풀이 문제 조회

					## 요청 형식
					- crosswordId: 십자말풀이 문제 세트 ID
					- 인증 토큰 필요 (JWT)

					## 응답
					- N x N 크기의 그리드 정보와 단어 요소(elements) 목록 반환
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
									  "code": "VOCA200_11",
									  "message": "성공적으로 십자말풀이를 조회했습니다.",
									  "result": {
									    "n": 5,
									    "elements": [
									      {
									        "id": 1,
									        "clueType": "ACROSS",
									        "wordLength": 5,
									        "verticalStartPoint": 0,
									        "horizontalStartPoint": 0,
									        "clueDescription": "달콤하고 둥근 과일"
									      }
									    ]
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.GetCrossword> getCrossword(
			@PathVariable Long crosswordId,
			@AuthenticationPrincipal AuthMember auth
	);

	@Operation(
			summary = "십자말풀이 한 문제 정답 제출 API By 김주헌",
			description = """
					# 십자말풀이 한 문제 정답 제출

					## 요청 형식
					- crosswordId: 십자말풀이 문제 세트 ID
					- crosswordHintId: 십자말풀이 힌트(요소) ID
					- answer: 사용자가 입력한 정답 문자열
					- 인증 토큰 필요 (JWT)

					## 응답
					- 해당 문제의 정답 여부(isCorrect), 전체 완료 여부 판단(hasNext), 클리어 시간(score, Duration) 반환
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
									  "code": "VOCA200_12",
									  "message": "성공적으로 십자말풀이 정답을 제출했습니다.",
									  "result": {
									    "isCorrect": true,
									    "hasNext": false,
									    "score": "PT1M30.5S"
									  }
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.SubmitCrossword> submitCrossword(
			@PathVariable Long crosswordId,
			@AuthenticationPrincipal AuthMember auth,
			@RequestParam Long crosswordHintId,
			@RequestParam String answer
	);
}
