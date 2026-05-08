package com.example.vocabook.domain.alert.controller.docs;

import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;

@Tag(name = "알림 관련 API")
public interface AlertControllerDocs {

    // FCM 토큰 등록
    @Operation(
            summary = "FCM 토큰 등록 API By 김주헌",
            description = """
                    # FCM 토큰 등록
                    클라이언트에서 발급한 FCM 토큰을 DB에 저장합니다
                    신규 등록 또는 기존 FCM 토큰을 대체할 수 있습니다.
                    
                    ## 주의 사항
                    - 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
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
                                        "code": "ALERT200_1",
                                        "message": "성공적으로 FCM 토큰을 등록했습니다.",
                                        "result": {
                                            "memberId": 6,
                                            "registeredAt": "2026-05-04T15:09:55.682673"
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
    ApiResponse<AlertResDTO.RegisterFcm> registerFcm(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody AlertReqDTO.RegisterFcm dto
    );

    // 알림 커스텀
    @Operation(
            summary = "알림 커스텀 API By 김주헌",
            description = """
                    # 알림 커스텀
                    알림을 커스텀합니다
                    내용 / 반복 주기 (일회성, 매일, 매주) / 알림 일시 를 정할 수 있습니다.
                    
                    ## 주의 사항
                    - 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
                    - 반복 주기에는 NONE, DAY, WEEK 만 허용합니다.
                    - 알림 일시의 포맷은 UTC 형식입니다. (2026-05-04T05:01:21.821Z)
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
                                        "code": "ALERT200_2",
                                        "message": "성공적으로 알림을 등록했습니다.",
                                        "result": {
                                            "message": "test",
                                            "repeat": "NONE",
                                            "alertedAt": "2026-05-04T15:13:00"
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
                    description = "실패 - 알람 등록 규칙 위반 (과거 시간 설정 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ALERT400_2",
                                      "message": "이미 지난 시간입니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - FCM 토큰이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ALERT404_2",
                                      "message": "FCM 토큰을 찾지 못했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "실패 - 중복 알람",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ALERT409_1",
                                      "message": "이미 설정된 알람이 존재합니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AlertResDTO.CustomAlert> customAlert(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody AlertReqDTO.CustomAlert dto
    );

    // 알림 목록 조회
    @Operation(
            summary = "알림 목록 조회 API By 김주헌",
            description = """
                    # 알림 목록 조회
                    자신이 등록한 커스텀 알림 목록을 커서 기반 페이징으로 조회합니다.
                    
                    ## 주의 사항
                    - 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
                    - 첫 번째 페이지 조회 시 cursor 값을 주지 않거나 "-1"을 입력하세요.
                    - pageSize 의 기본값은 10입니다.
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
                                        "code": "ALERT200_3",
                                        "message": "성공적으로 알림 목록을 조회했습니다.",
                                        "result": {
                                            "data": [
                                                {
                                                    "id": 1,
                                                    "message": "test",
                                                    "repeat": "NONE",
                                                    "alertedAt": "2026-05-04T15:13:00"
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
            )
    })
    ApiResponse<PagingResDTO.Cursor<AlertResDTO.AlertList>> getAlertList(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    );

    // 알림 삭제
    @Operation(
            summary = "알림 삭제 API By 김주헌",
            description = """
                    # 알림 삭제
                    특정 알림을 삭제합니다.
                    
                    ## 주의 사항
                    - 반드시 로그인을 먼저 해야 합니다 (JWT 토큰 필수)
                    - 자신이 직접 설정한 알림만 삭제할 수 있습니다.
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
                                        "code": "ALERT200_4",
                                        "message": "성공적으로 알림을 삭제했습니다.",
                                        "result": {
                                            "id": 1,
                                            "deletedAt": "2026-05-04T15:15:00.000000"
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
                    description = "실패 - 알림을 찾을 수 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ALERT404_1",
                                      "message": "알림을 찾지 못했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 알림을 등록한 사용자와 다른 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ALERT400_3",
                                      "message": "알림 설정한 사용자가 아닙니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AlertResDTO.DeleteAlert> deleteAlert(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long alertId
    );
}
