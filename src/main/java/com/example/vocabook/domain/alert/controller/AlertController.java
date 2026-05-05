package com.example.vocabook.domain.alert.controller;

import com.example.vocabook.domain.alert.code.AlertSuccessCode;
import com.example.vocabook.domain.alert.controller.docs.AlertControllerDocs;
import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.service.AlertService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlertController implements AlertControllerDocs {

    private final AlertService alertService;

    // FCM 토큰 등록
    @PostMapping("/v1/fcm")
    public ApiResponse<AlertResDTO.RegisterFcm> registerFcm(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody AlertReqDTO.RegisterFcm dto
    ) {
        BaseSuccessCode code = AlertSuccessCode.REGISTER_FCM;
        return ApiResponse.onSuccess(code, alertService.registerFcm(auth, dto));
    }

    // 알림 커스텀
    @PostMapping("/v1/alerts/custom")
    public ApiResponse<AlertResDTO.CustomAlert> customAlert(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody AlertReqDTO.CustomAlert dto
    ) {
        BaseSuccessCode code = AlertSuccessCode.CUSTOM_ALERT;
        return ApiResponse.onSuccess(code, alertService.customAlert(auth, dto));
    }
}
