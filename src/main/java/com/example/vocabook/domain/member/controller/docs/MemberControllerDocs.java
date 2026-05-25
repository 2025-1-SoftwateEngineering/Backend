package com.example.vocabook.domain.member.controller.docs;

import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.domain.member.enums.PhotoType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 관련 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "내 프로필 조회 API By 김주헌",
            description = """
                    # 내 프로필 조회
                    JWT 토큰을 이용해 내 정보를 조회합니다
                    
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
                                      "code": "MEMBER200_1",
                                      "message": "성공적으로 사용자 프로필을 조회했습니다.",
                                      "result": {
                                        "nickname": "User",
                                        "email": "user@example.com",
                                        "streak": 0,
                                        "totalStudyDays": 0,
                                        "coin": 0,
                                        "authorize": "ROLE_USER",
                                        "activeProfilePhoto": "PROFILE_PHOTO_1",
                                        "activeProfileBg": null
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
    ApiResponse<MemberResDTO.MyProfile> getMyProfile(
            @AuthenticationPrincipal AuthMember auth
    );

    // 친구 요청 목록 조회
    @Operation(
            summary = "친구 요청 목록 조회 API By 김주헌",
            description = """
                    # 친구 요청 목록 조회
                    지금까지 들어온 친구 요청 목록을 조회합니다
                    커서 기반 페이지네이션을 지원합니다
                    
                    ## 주의사항
                    - 커서의 초기값은 -1 입니다
                    - 불러올 데이터 사이즈(pageSize)는 양수여야합니다
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
                                      "code": "MEMBER200_1",
                                      "message": "성공적으로 사용자 프로필을 조회했습니다.",
                                      "result": {
                                        "nickname": "User",
                                        "email": "user@example.com",
                                        "streak": 0,
                                        "coin": 0
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 커서가 잘못된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_1",
                                      "message": "잘못된 커서값을 입력했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 범위를 넘어선 페이지 사이즈",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "isSuccess": false,
                                        "code": "COMMON400_1",
                                        "message": "요청을 처리하지 못했습니다.",
                                        "result": {
                                            "pageSize": "조회할 데이터 수는 양의 정수 범위로 입력해주세요"
                                        }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 잘못 입력한 페이지 사이즈 (타입 미스)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "isSuccess": false,
                                        "code": "COMMON400_2",
                                        "message": "타입을 잘못 입력했습니다.",
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
            )
    })
    ApiResponse<PagingResDTO.Cursor<MemberResDTO.FriendRequestList>> getFriendRequestList(
            @RequestParam(defaultValue = "-1") @NotBlank(message = "커서는 빈칸일 수 없습니다.") String cursor,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "조회할 데이터 수는 양의 정수 범위로 입력해주세요")
            @Max(value = 100, message = "조회할 데이터 수는 100이하입니다.") Integer pageSize,
            @AuthenticationPrincipal AuthMember auth
    );

    // 친구 요청 보내기 (알림 전송해야 함)
    @Operation(
            summary = "친구 요청 보내기 API By 김주헌",
            description = """
                    # 친구 요청 보내기
                    사용자의 ID를 기반으로 친구 요청을 보냅니다
                    사용자 간단 조회 -(ID 파악)-> 친구 요청 보내기
                    
                    ## 주의사항
                    - 반드시 로그인을 해야합니다
                    - 요청 보낼 상대의 ID를 반드시 입력해야 합니다
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
                                      "code": "MEMBER200_3",
                                      "message": "성공적으로 친구 요청을 보냈습니다.",
                                      "result": {
                                        "id": 8,
                                        "nickname": "user3"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "실패 - 이미 친구 요청을 보냈거나 친구인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER409_1",
                                      "message": "이미 친구 요청을 보냈거나 이미 친구입니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "friendId",
            description = "친구 요청을 보낼 사용자 ID",
            required = true,
            example = "1"
    )
    ApiResponse<MemberResDTO.SendFriendRequest> sendFriendRequest(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable @NotNull(message = "요청 보낼 친구 ID는 필수입니다.")
            Long friendId
    );

    // 친구 요청 상태 변경
    @Operation(
            summary = "친구 요청 상태 변경 API By 김주헌",
            description = """
                    # 친구 요청 상태 변경
                    들어온 친구 요청을 수락할지 거절할지 상태를 변경합니다
                    
                    ## 사용 방법
                    - 상태: accept, reject (대소문자 구분 X)
                    
                    ## 주의사항
                    - 반드시 로그인을 해야합니다
                    - 상대가 나에게 친구 요청을 보내야 하며 수락은 친구 ID를 입력해야 합니다
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
                                      "code": "MEMBER200_4",
                                      "message": "성공적으로 친구 요청 상태를 변경했습니다.",
                                      "result": {
                                        "id": 7,
                                        "nickname": "user2",
                                        "state": "ACCEPTED"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 친구 요청이 대기상태가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_3",
                                      "message": "친구 요청 대기 상태가 아닙니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 친구 요청을 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_2",
                                      "message": "친구 요청을 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "friendId",
            description = "친구 요청을 보낸 사용자 ID",
            required = true,
            example = "1"
    )
    ApiResponse<MemberResDTO.UpdateFriendRequest> updateFriendRequest(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable(name = "friendId") @NotNull(message = "요청 보낸 친구 ID는 필수입니다.")
            Long fromMemberId,
            @RequestBody @Valid MemberReqDTO.UpdateFriendRequest dto
    );

    // 사용자 단순 조회
    @Operation(
            summary = "사용자 단순 조회 API By 김주헌",
            description = """
                    # 사용자 단순 조회
                    이메일로 사용자 정보를 조회합니다
                    민감한 정보는 제외한 정보로, 친구 요청을 위한 ID가 포함되어 있습니다.
                    
                    ## 주의사항
                    - 반드시 로그인을 해야합니다
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
                                      "code": "MEMBER200_5",
                                      "message": "성공적으로 사용자를 조회했습니다.",
                                      "result": {
                                        "id": 6,
                                        "nickname": "User",
                                        "email": "user@example.com"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    ApiResponse<MemberResDTO.SearchMember> searchMember(
            @RequestBody @Valid MemberReqDTO.SearchMember dto
    );

    // 친구 목록 조회
    @Operation(
            summary = "친구 목록 조회 API By 김주헌",
            description = """
                    # 친구 목록 조회
                    친구 목록을 조회합니다
                    커서 기반 페이지네이션을 지원합니다
                    
                    ## 주의사항
                    - 커서의 초기값은 -1 입니다
                    - 불러올 데이터 사이즈(pageSize)는 양수여야합니다
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
                                      "code": "MEMBER200_2",
                                      "message": "성공적으로 친구 목록을 조회했습니다.",
                                      "result": {
                                        "data": [
                                          {
                                            "toMemberId": 7,
                                            "state": "ACCEPTED"
                                          },
                                          {
                                            "toMemberId": 7,
                                            "state": "ACCEPTED"
                                          },
                                          {
                                            "toMemberId": 6,
                                            "state": "ACCEPTED"
                                          }
                                        ],
                                        "nextCursor": "4",
                                        "hasNext": false,
                                        "pageSize": 3
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 커서가 잘못된 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_1",
                                      "message": "잘못된 커서값을 입력했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 범위를 넘어선 페이지 사이즈",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "isSuccess": false,
                                        "code": "COMMON400_1",
                                        "message": "요청을 처리하지 못했습니다.",
                                        "result": {
                                            "pageSize": "조회할 데이터 수는 양의 정수 범위로 입력해주세요"
                                        }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 잘못 입력한 페이지 사이즈 (타입 미스)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "isSuccess": false,
                                        "code": "COMMON400_2",
                                        "message": "타입을 잘못 입력했습니다.",
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
            )
    })
    ApiResponse<PagingResDTO.Cursor<MemberResDTO.FriendList>> getFriendList(
            @RequestParam(defaultValue = "-1") @NotBlank(message = "커서는 빈칸일 수 없습니다.")
            String cursor,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "조회할 데이터 수는 양의 정수 범위로 입력해주세요")
            @Max(value = 100, message = "조회할 데이터 수는 100이하입니다.")
            Integer pageSize,
            @AuthenticationPrincipal AuthMember auth
    );

    // 친구 프로필 조회
    @Operation(
            summary = "친구 프로필 조회 API By 김주헌",
            description = """
                    # 친구 프로필 조회
                    친구 프로필을 조회합니다
                    
                    ## 주의사항
                    - 대상과 친구관계여야 합니다
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
                                      "code": "MEMBER200_1",
                                      "message": "성공적으로 사용자 프로필을 조회했습니다.",
                                      "result": {
                                        "id": 7,
                                        "nickname": "user2",
                                        "streak": 0,
                                        "totalStudyDays": 0,
                                        "coin": 0,
                                        "loginAt": "2026-04-28T12:29:54.074504"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 친구관계가 아닌 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_4",
                                      "message": "대상과 친구가 아닙니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "friendId",
            description = "프로필을 조회할 친구 사용자 ID",
            example = "1",
            required = true
    )
    ApiResponse<MemberResDTO.FriendProfile> getFriendProfile(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long friendId
    );

    // 친구 차단
    @Operation(
            summary = "친구 차단 API By 김주헌",
            description = """
                    # 친구 차단
                    친구를 차단합니다
                    
                    ## 주의사항
                    - 대상과 친구관계여야 합니다
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
                                      "code": "MEMBER200_6",
                                      "message": "성공적으로 사용자를 차단했습니다.",
                                      "result": {
                                        "id": 7,
                                        "nickname": "user2",
                                        "blockedAt": "2026-04-29T12:12:49.81384"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 친구관계가 아닌 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_4",
                                      "message": "대상과 친구가 아닙니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "friendId",
            description = "차단할 친구 사용자 ID",
            example = "1",
            required = true
    )
    ApiResponse<MemberResDTO.Blocking> blockMember(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long friendId
    );

    // 사용자 신고
    @Operation(
            summary = "사용자 신고 API By 김주헌",
            description = """
                    # 사용자 신고
                    특정 사용자를 신고합니다. 신고 사유와 상세 사유를 입력할 수 있습니다.
                    
                    ## 주의사항
                    - 반드시 로그인을 해야 합니다.
                    - 자기 자신을 신고할 수 없습니다.
                    - 이미 신고한 사용자를 중복 신고할 수 없습니다.
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
                                      "code": "MEMBER200_7",
                                      "message": "성공적으로 사용자를 신고했습니다.",
                                      "result": {
                                        "targetMemberId": 2,
                                        "reason": "SPAM",
                                        "detailReason": "광고 도배",
                                        "reportedAt": "2026-05-13T01:45:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 자기 자신 신고",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_5",
                                      "message": "자기 자신을 신고할 수 없습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "208",
                    description = "실패 - 이미 신고한 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER208_1",
                                      "message": "이미 해당 사용자를 신고했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "memberId",
            description = "신고할 사용자 ID",
            example = "2",
            required = true
    )
    ApiResponse<MemberResDTO.ReportMember> reportMember(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long memberId,
            @RequestBody @Valid MemberReqDTO.ReportMember dto
    );

    // 사진 업로드용 URL 생성
    @Operation(
            summary = "사진 업로드용 URL 발급 API By 김주헌",
            description = """
                    # 사진 업로드용 URL 발급
                    GCS(Google Cloud Storage)에 사진을 업로드하기 위한 Signed URL을 발급받습니다.
                    
                    ## 주의사항
                    - 반드시 로그인을 해야 합니다.
                    - 확장자가 포함된 파일명을 입력해야 합니다 (예: image.png).
                    - 허용된 확장자(png, jpg, jpeg, webp)만 업로드 가능합니다.
                    - 일반 사용자는 PROFILE 타입만 업로드 가능하며, 관리자는 모든 타입(PROFILE, ITEM, BACKGROUND, PET) 업로드 가능합니다.
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
                                      "code": "MEMBER200_8",
                                      "message": "성공적으로 URL을 발급했습니다.",
                                      "result": {
                                        "fileName": "550e8400-e29b-41d4-a716-446655440000.png",
                                        "signedUrl": "https://storage.googleapis.com/vocabook-bucket/profile/550e8400-e29b-41d4-a716-446655440000.png?X-Goog-Algorithm=GOOG4-RSA-SHA256...",
                                        "photoType": "PROFILE"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 지원하지 않는 파일 형식 또는 권한 없는 타입",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_6",
                                      "message": "지원하지 않는 파일 형식이거나 권한이 없는 타입입니다.",
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
            )
    })
    @Parameter(
            name = "fileName",
            description = "업로드할 파일명 (확장자 포함)",
            example = "profile.png",
            required = true
    )
    @Parameter(
            name = "photoType",
            description = "사진 타입 (PROFILE, ITEM, BACKGROUND, PET)",
            example = "PROFILE",
            required = true
    )
    ApiResponse<MemberResDTO.CreateSignedUri> createSignedUri(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam String fileName,
            @RequestParam PhotoType photoType
    );

    // 사진 업로드 완료
    @Operation(
            summary = "사진 업로드 완료 API By 김주헌",
            description = """
                    # 사진 업로드 완료
                    Signed URL을 통해 GCS에 업로드를 마친 후, 해당 파일명을 서버에 등록(완료) 처리합니다.
                    
                    ## 주의사항
                    - 반드시 로그인을 해야 합니다.
                    - 발급받았던 uuid 형태의 파일명(fileName)을 정확히 입력해야 합니다.
                    - PROFILE 변경 시 targetId는 필요하지 않지만, 다른 타입(ITEM, BACKGROUND, PET)은 대상 targetId가 필수입니다.
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
                                      "code": "MEMBER200_9",
                                      "message": "성공적으로 이미지를 등록했습니다.",
                                      "result": {
                                        "objectName": "profile/550e8400-e29b-41d4-a716-446655440000.png",
                                        "publicUrl": "https://storage.googleapis.com/vocabook-bucket/profile/550e8400-e29b-41d4-a716-446655440000.png"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 대상 ID가 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER400_7",
                                      "message": "대상 ID가 필요합니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 업로드된 파일을 찾지 못했거나 대상을 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_3",
                                      "message": "업로드된 파일을 찾지 못했거나 등록 대상을 찾지 못했습니다.",
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
            )
    })
    @Parameter(
            name = "fileName",
            description = "발급받았던 UUID 파일명",
            example = "550e8400-e29b-41d4-a716-446655440000.png",
            required = true
    )
    @Parameter(
            name = "photoType",
            description = "사진 타입 (PROFILE, ITEM, BACKGROUND, PET)",
            example = "PROFILE",
            required = true
    )
    @Parameter(
            name = "targetId",
            description = "등록 대상 ID (PROFILE 외의 타입에 필수)",
            example = "1",
            required = false
    )
    ApiResponse<MemberResDTO.UploadImage> uploadImage(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam String fileName,
            @RequestParam PhotoType photoType,
            @RequestParam(required = false) Long targetId
    );

    // 프로필 수정
    @Operation(
            summary = "프로필 수정 API By 김주헌",
            description = """
                    # 프로필 수정
                    사용자의 닉네임과 이메일을 수정합니다.
                    
                    ## 주의사항
                    - 반드시 로그인을 해야 합니다.
                    - 비밀번호 확인(confirmPassword)은 필수입니다.
                    - 변경을 원하지 않는 필드(nickname, email)는 비워두면 기존 값이 유지됩니다.
                    - 프로필 사진 변경은 `사진 업로드용 URI 발급 API` -> `사진 업로드 완료 API`를 순서대로 사용해주세요.
                    - 이메일을 변경할 경우 기존 JWT 토큰은 만료(무효화)되므로 다시 로그인해야 합니다.
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
                                      "code": "MEMBER200_10",
                                      "message": "성공적으로 프로필을 수정했습니다.",
                                      "result": {
                                        "email": "updated@example.com",
                                        "nickname": "UpdatedNickname"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 비밀번호가 일치하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH400_2",
                                      "message": "아이디 혹은 비밀번호가 잘못되었습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 사용자를 찾지 못한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "MEMBER404_1",
                                      "message": "해당 사용자를 찾지 못했습니다.",
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
            )
    })
    ApiResponse<MemberResDTO.UpdateProfile> updateProfile(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody @Valid MemberReqDTO.UpdateProfile dto
    );
}
