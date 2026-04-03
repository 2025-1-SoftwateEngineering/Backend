package com.example.vocabook.domain.member.controller;

import com.example.vocabook.domain.member.code.AuthSuccessCode;
import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.AuthResDTO;
import com.example.vocabook.domain.member.service.AuthService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/v1/sign-up")
    public ApiResponse<AuthResDTO.SignUp> signUp(
            @RequestBody @Valid AuthReqDTO.SignUp dto
    ){
        BaseSuccessCode code = AuthSuccessCode.SIGN_UP;
        return ApiResponse.onSuccess(code, authService.signUp(dto));
    }

    // 로그인
    @PostMapping("/v1/login")
    public ApiResponse<AuthResDTO.Login> login(
            @RequestBody @Valid AuthReqDTO.Login dto
    ){
        BaseSuccessCode code = AuthSuccessCode.LOGIN;
        return ApiResponse.onSuccess(code, authService.login(dto));
    }

    // 토큰 재발급
    @PostMapping("/v1/reissue")
    public ApiResponse<AuthResDTO.Reissue> reissue(
            @RequestBody @Valid AuthReqDTO.Reissue dto
    ){
        BaseSuccessCode code = AuthSuccessCode.REISSUE;
        return ApiResponse.onSuccess(code, authService.reissue(dto));
    }
}
