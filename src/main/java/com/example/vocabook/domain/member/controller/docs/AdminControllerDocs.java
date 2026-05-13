package com.example.vocabook.domain.member.controller.docs;

import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.dto.res.AdminResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "관리자 전용 API")
public interface AdminControllerDocs {

    @Operation(
            summary = "신고 목록 조회 API By 김주헌",
            description = """
                    # 신고 목록 조회
                    전체 신고 목록을 무한 스크롤(커서) 방식으로 조회합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능이므로 일반 사용자는 접근할 수 없습니다.
                    - 첫 번째 페이지 조회 시 cursor 값을 주지 않거나 "-1"을 입력하세요.
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
                                      "code": "ADMIN200_1",
                                      "message": "성공적으로 신고 목록을 조회했습니다.",
                                      "result": {
                                        "data": [
                                          {
                                            "targetMemberId": 2,
                                            "reason": "SPAM",
                                            "detailReason": "광고성 글 도배",
                                            "reportCnt": 5
                                          }
                                        ],
                                        "nextCursor": "5:2",
                                        "hasNext": false,
                                        "totalElements": 1
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 올바르지 않은 커서 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON400_3",
                                      "message": "커서를 잘못입력했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<PagingResDTO.Cursor<AdminResDTO.ReportList>> getReportList(
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    );

    @Operation(
            summary = "사용자 영구 차단 API By 김주헌",
            description = """
                    # 사용자 영구 차단
                    특정 사용자를 영구 차단하고, 해당 사용자에 대한 신고 내역을 모두 삭제합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 영구 차단된 사용자는 더 이상 서비스에 접근할 수 없습니다.
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
                                      "code": "ADMIN200_2",
                                      "message": "성공적으로 해당 유저를 영구 차단했습니다.",
                                      "result": {
                                        "targetMemberId": 2
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 존재하지 않는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾을 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.Suspend> suspend(
            @PathVariable Long memberId
    );

    @Operation(
            summary = "단어장 추가 API By 김주헌",
            description = """
                    # 단어장 추가
                    새로운 단어장 목록을 한 번에 다수 추가합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - description과 solvedCoin을 필수로 입력해야 합니다.
                    - 기존에 동일한 설명과 코인을 가진 단어장이 있다면 제외하고 저장됩니다.
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
                                      "code": "ADMIN200_3",
                                      "message": "성공적으로 단어장을 추가했습니다.",
                                      "result": [
                                        {
                                          "id": 1,
                                          "addedAt": "2026-05-13T01:10:00"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "성공 (추가될 내용 없음) - 모든 항목이 중복됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": true,
                                      "code": "ADMIN204_1",
                                      "message": "추가될 단어장이 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<List<AdminResDTO.AddVocabulary>> addVocabulary(
            @RequestBody @Valid List<AdminReqDTO.AddVocabulary> dto
    );

    @Operation(
            summary = "단어장 목록 조회 API By 김주헌",
            description = """
                    # 단어장 목록 조회
                    등록된 단어장 목록과 각 단어장에 속한 단어들을 조회합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 첫 번째 페이지 조회 시 cursor 값을 주지 않거나 "-1"을 입력하세요.
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
                                      "code": "ADMIN200_4",
                                      "message": "성공적으로 단어장 목록을 조회했습니다.",
                                      "result": {
                                        "data": [
                                          {
                                            "id": 1,
                                            "description": "토익 빈출 단어장",
                                            "addedAt": "2026-05-13T01:10:00",
                                            "wordList": [
                                              { "english": "apple", "meaning": "사과" }
                                            ]
                                          }
                                        ],
                                        "nextCursor": "1",
                                        "hasNext": false,
                                        "totalElements": 1
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 올바르지 않은 커서 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON400_3",
                                      "message": "커서를 잘못입력했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<PagingResDTO.Cursor<AdminResDTO.GetVocabularyList>> getVocabularyList(
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    );

    @Operation(
            summary = "단어장 수정 API By 김주헌",
            description = """
                    # 단어장 수정
                    특정 단어장의 설명(description)과 보상 코인(solvedCoin)을 수정합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 존재하지 않는 단어장 ID를 요청하면 404 에러가 발생합니다.
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
                                      "code": "ADMIN200_7",
                                      "message": "성공적으로 단어장을 수정했습니다.",
                                      "result": {
                                        "id": 1,
                                        "description": "수정된 토익 단어장",
                                        "solvedCoin": 200
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
                                      "code": "ADMIN404_1",
                                      "message": "해당 단어장을 찾을 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.UpdateVocabulary> updateVocabulary(
            @PathVariable Long vocaId,
            @RequestBody @Valid AdminReqDTO.UpdateVocabulary dto
    );

    @Operation(
            summary = "단어장 삭제 API By 김주헌",
            description = """
                    # 단어장 삭제
                    특정 단어장을 삭제합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 존재하지 않는 단어장 ID를 요청하면 404 에러가 발생합니다.
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
                                      "code": "ADMIN200_8",
                                      "message": "성공적으로 단어장을 삭제했습니다.",
                                      "result": {
                                        "id": 1
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
                                      "code": "ADMIN404_1",
                                      "message": "해당 단어장을 찾을 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.DeleteVocabulary> deleteVocabulary(
            @PathVariable Long vocaId
    );

    @Operation(
            summary = "단어 추가 API By 김주헌",
            description = """
                    # 단어 추가
                    새로운 단어들을 특정 단어장 혹은 독립적으로 다수 추가합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - vocabularyId를 빈 값으로 두면 단어장에 속하지 않은 단어로 생성됩니다.
                    - 동일한 뜻과 스펠링을 가진 단어가 이미 있다면 중복 저장되지 않습니다.
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
                                      "code": "ADMIN200_5",
                                      "message": "성공적으로 단어를 추가했습니다.",
                                      "result": [
                                        {
                                          "id": 1,
                                          "english": "apple",
                                          "meaning": "사과",
                                          "vocabularyId": 1
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<List<AdminResDTO.AddWord>> addWord(
            @RequestBody @Valid List<AdminReqDTO.AddWord> dto
    );

    @Operation(
            summary = "단어 검색 API By 김주헌",
            description = """
                    # 단어 검색
                    영단어(english)를 쿼리 파라미터로 받아 DB에 해당 단어가 존재하는지 검색합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 첫 번째로 검색된 단어 하나만 반환합니다.
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
                                      "code": "ADMIN200_6",
                                      "message": "성공적으로 단어를 찾았습니다.",
                                      "result": {
                                        "id": 1,
                                        "english": "apple",
                                        "meaning": "사과",
                                        "vocabularyId": 1
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.SearchWord> searchWord(
            @RequestParam String word
    );

    @Operation(
            summary = "단어 수정 API By 김주헌",
            description = """
                    # 단어 수정
                    특정 단어의 영어, 뜻, 그리고 소속 단어장을 수정합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 존재하지 않는 단어 또는 단어장을 입력하면 404 에러가 발생합니다.
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
                                      "code": "ADMIN200_9",
                                      "message": "성공적으로 단어를 수정했습니다.",
                                      "result": {
                                        "id": 1,
                                        "english": "banana",
                                        "meaning": "바나나",
                                        "vocabularyId": 1
                                      }
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
                                      "code": "ADMIN404_2",
                                      "message": "해당 단어를 찾을 수 없습니다.",
                                      "result": null
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
                                      "code": "ADMIN404_1",
                                      "message": "해당 단어장을 찾을 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.UpdateWord> updateWord(
            @PathVariable Long wordId,
            @RequestBody @Valid AdminReqDTO.UpdateWord dto
    );

    @Operation(
            summary = "단어 삭제 API By 김주헌",
            description = """
                    # 단어 삭제
                    특정 단어를 삭제합니다.
                    
                    ## 주의 사항
                    - 관리자 전용 기능입니다.
                    - 존재하지 않는 단어를 요청하면 404 에러가 발생합니다.
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
                                      "code": "ADMIN200_10",
                                      "message": "성공적으로 단어를 삭제했습니다.",
                                      "result": {
                                        "id": 1
                                      }
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
                                      "code": "ADMIN404_2",
                                      "message": "해당 단어를 찾을 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "실패 - 관리자 권한이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON403_1",
                                      "message": "접근이 제한되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AdminResDTO.DeleteWord> deleteWord(
            @PathVariable Long wordId
    );
}
