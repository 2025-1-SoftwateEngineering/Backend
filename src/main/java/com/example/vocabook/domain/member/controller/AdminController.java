package com.example.vocabook.domain.member.controller;

import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/v1/test")
    public ApiResponse<String> test() {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, "adminTest");
    }
}
