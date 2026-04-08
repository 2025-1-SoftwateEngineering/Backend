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
			summary = "단어 목록 조회 API By 윤민재",
			description = """
					# 단어 목록 조회

					## 요청 형식
					- vocaId: 단어장 ID
					- page: 페이지 번호 (0부터 시작, 기본값 0)
					- 10개씩 페이징
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
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 존재하지 않는 단어장",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "VOCA404_1",
									  "message": "단어장을 찾을 수 없습니다.",
									  "result": null
									}
									""")
					)
			)
	})
	ApiResponse<VocaResDTO.WordList> getWords(
			@PathVariable Long vocaId,
			@RequestParam(defaultValue = "0") int page
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
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 존재하지 않는 단어장",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "VOCA404_1",
									  "message": "단어장을 찾을 수 없습니다.",
									  "result": null
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
									    "earnedCoin": 10,
									    "results": [
									      {
									        "wordId": 1,
									        "meaning": "사과",
									        "correctAnswer": "apple",
									        "submittedAnswer": "apple",
									        "isCorrect": true
									      },
									      {
									        "wordId": 2,
									        "meaning": "바나나",
									        "correctAnswer": "banana",
									        "submittedAnswer": "bananaa",
									        "isCorrect": false
									      }
									    ]
									  }
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 존재하지 않는 단어장",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "VOCA404_1",
									  "message": "단어장을 찾을 수 없습니다.",
									  "result": null
									}
									""")
					)
			),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(
					responseCode = "404",
					description = "실패 - 존재하지 않는 단어",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ApiResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": false,
									  "code": "VOCA404_2",
									  "message": "단어를 찾을 수 없습니다.",
									  "result": null
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