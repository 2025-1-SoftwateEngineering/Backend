package com.example.vocabook.domain.alert.controller;

import com.example.vocabook.domain.alert.code.AlertSuccessCode;
import com.example.vocabook.domain.alert.controller.docs.AlertControllerDocs;
import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.service.AlertService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 알림 목록 조회
    @GetMapping("/v1/alerts")
    public ApiResponse<PagingResDTO.Cursor<AlertResDTO.AlertList>> getAlertList(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        BaseSuccessCode code = AlertSuccessCode.GET_ALERT_LIST;
        return ApiResponse.onSuccess(code, alertService.getAlertList(auth, cursor, pageSize));
    }

    // 알림 삭제
    @DeleteMapping("/v1/alerts/{alertId}")
    public ApiResponse<AlertResDTO.DeleteAlert> deleteAlert(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long alertId
    ) {
        BaseSuccessCode code = AlertSuccessCode.DELETE_ALERT;
        return ApiResponse.onSuccess(code, alertService.deleteAlert(auth, alertId));
    }
}
