package com.example.vocabook.domain.member.controller.docs;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.AuthResDTO;
import com.example.vocabook.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "회원가입 API By 김주헌",
            description = """
                    # 회원가입
                    
                    ## 요청 형식
                    - 이메일: `example@example.com` 형식
                    - 비밀번호: raw 형태, 제한 X
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
                                      "code": "AUTH200_1",
                                      "message": "성공적으로 회원가입을 마쳤습니다.",
                                      "result": {
                                        "accessToken": "eyJhbGciOiJ...",
                                        "refreshToken": "eyJhbGciO...",
                                        "refreshExpiration": "2026-04-17T14:28:13.000Z"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "실패 - 이미 등록된 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH409_1",
                                      "message": "이미 회원가입된 사용자입니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AuthResDTO.SignUp> signUp(@RequestBody @Valid AuthReqDTO.SignUp dto);

    @Operation(
            summary = "로그인 API By 김주헌",
            description = """
                    # 로그인
                    
                    ## 요청 형식
                    - 이메일: `example@example.com` 형식
                    - 비밀번호: raw 형태, 제한 X
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
                                      "code": "AUTH200_2",
                                      "message": "성공적으로 로그인했습니다.",
                                      "result": {
                                        "accessToken": "eyJhbGciO~",
                                        "refreshToken": "eyJhbGciO~",
                                        "refreshExpiration": "2026-04-17T14:32:57.000Z"
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
                                      "message": "해당 사용자를 찾지 못했습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - 아이디 혹은 비밀번호 불일치",
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
                    responseCode = "400",
                    description = "실패 - 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON400_1",
                                      "message": "요청을 처리하지 못했습니다.",
                                      "result": {
                                        "email": "이메일 형식을 입력해주세요."
                                      }
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AuthResDTO.Login> login(@RequestBody @Valid AuthReqDTO.Login dto);

    @Operation(
            summary = "토큰 재발급 API By 김주헌",
            description = """
                    # 토큰 재발급
                    
                    ## 요청 형식
                    - 리프레시 토큰: 정상적인 JWT 토큰이여야 함
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
                                      "code": "AUTH200_3",
                                      "message": "성공적으로 토큰을 재발급했습니다.",
                                      "result": {
                                        "accessToken": "eyJhbGciO~",
                                        "refreshToken": "eyJhbGciO~",
                                        "refreshExpiration": "2026-04-17T14:36:23.000Z"
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "실패 - 리프레시 토큰이 아님",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH400_3",
                                      "message": "리프레시 토큰이 아닙니다.",
                                      "result": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "실패 - DB에 저장되어 있는 리프레시 토큰과 맞지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isSuccess": false,
                                      "code": "AUTH400_4",
                                      "message": "리프레시 트큰이 맞지 않습니다.",
                                      "result": null
                                    }
                                    """)
                    )
            )
    })
    ApiResponse<AuthResDTO.Reissue> reissue(@RequestBody @Valid AuthReqDTO.Reissue dto);
}
