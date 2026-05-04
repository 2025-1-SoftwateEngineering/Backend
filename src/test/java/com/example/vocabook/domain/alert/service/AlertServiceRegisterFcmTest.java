package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService FCM 토큰 등록 테스트")
public class AlertServiceRegisterFcmTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private AuthMember authMember;
    private Member myMember;
    private Alert myAlert;

    @BeforeEach
    void setUp() {
        myMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        authMember = new AuthMember(myMember);
        
        myAlert = Alert.builder()
                .fcmToken("old-token")
                .member(myMember)
                .build();
    }

    @Test
    @DisplayName("FCM 토큰 등록 성공 - 기존 토큰이 없을 때 새로 등록")
    void registerFcm_Success_NewToken() {
        // given
        AlertReqDTO.RegisterFcm dto = new AlertReqDTO.RegisterFcm("new-token");
        given(alertRepository.findByMember(myMember)).willReturn(Optional.empty());
        given(alertRepository.save(any(Alert.class))).willReturn(myAlert);

        // when
        AlertResDTO.RegisterFcm res = alertService.registerFcm(authMember, dto);

        // then
        assertNotNull(res);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("FCM 토큰 등록 성공 - 기존 토큰 업데이트")
    void registerFcm_Success_UpdateToken() {
        // given
        AlertReqDTO.RegisterFcm dto = new AlertReqDTO.RegisterFcm("updated-token");
        given(alertRepository.findByMember(myMember)).willReturn(Optional.of(myAlert));

        // when
        AlertResDTO.RegisterFcm res = alertService.registerFcm(authMember, dto);

        // then
        assertNotNull(res);
        assertEquals("updated-token", myAlert.getFcmToken());
        verify(alertRepository, times(0)).save(any(Alert.class));
    }
}
