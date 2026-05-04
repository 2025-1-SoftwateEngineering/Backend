package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
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
import org.quartz.SchedulerException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService 커스텀 알림 등록 및 예외 테스트")
public class AlertServiceCustomAlertTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertDetailRepository alertDetailRepository;

    @Mock
    private AlertScheduleService alertScheduleService;

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
    @DisplayName("커스텀 알림 등록 성공")
    void customAlert_Success() throws SchedulerException {
        // given
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        AlertReqDTO.CustomAlert dto = new AlertReqDTO.CustomAlert("공부할 시간입니다!", Repeat.NONE, futureTime);
        
        given(alertRepository.findByMember(myMember)).willReturn(Optional.of(myAlert));
        given(alertDetailRepository.existsByContentAndAlertAndRepeatAndAlertedAt(any(), any(), any(), any())).willReturn(false);

        // when
        AlertResDTO.CustomAlert res = alertService.customAlert(authMember, dto);

        // then
        assertNotNull(res);
        assertEquals("공부할 시간입니다!", res.message());
        verify(alertDetailRepository, times(1)).save(any());
        verify(alertScheduleService, times(1)).schedule(any());
    }

    @Test
    @DisplayName("커스텀 알림 등록 실패 - FCM 토큰 없음 (NOT_FOUND_FCM)")
    void customAlert_Fail_NotFoundFcm() {
        // given
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        AlertReqDTO.CustomAlert dto = new AlertReqDTO.CustomAlert("test", Repeat.NONE, futureTime);
        
        given(alertRepository.findByMember(myMember)).willReturn(Optional.empty());

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.customAlert(authMember, dto)
        );

        // then
        assertEquals(AlertErrorCode.NOT_FOUND_FCM, exception.getCode());
    }

    @Test
    @DisplayName("커스텀 알림 등록 실패 - 중복된 알림 존재 (ALREADY_SET_ALERT)")
    void customAlert_Fail_AlreadySetAlert() {
        // given
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        AlertReqDTO.CustomAlert dto = new AlertReqDTO.CustomAlert("test", Repeat.NONE, futureTime);
        
        given(alertRepository.findByMember(myMember)).willReturn(Optional.of(myAlert));
        given(alertDetailRepository.existsByContentAndAlertAndRepeatAndAlertedAt(any(), any(), any(), any())).willReturn(true);

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.customAlert(authMember, dto)
        );

        // then
        assertEquals(AlertErrorCode.ALREADY_SET_ALERT, exception.getCode());
    }

    @Test
    @DisplayName("커스텀 알림 등록 실패 - 과거 시간으로 일회성 알림 설정 (INVADED_ALERT_TIME)")
    void customAlert_Fail_InvadedAlertTime() {
        // given
        OffsetDateTime pastTime = OffsetDateTime.now().minusHours(1); // 1시간 전
        AlertReqDTO.CustomAlert dto = new AlertReqDTO.CustomAlert("test", Repeat.NONE, pastTime);
        
        given(alertRepository.findByMember(myMember)).willReturn(Optional.of(myAlert));
        given(alertDetailRepository.existsByContentAndAlertAndRepeatAndAlertedAt(any(), any(), any(), any())).willReturn(false);

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.customAlert(authMember, dto)
        );

        // then
        assertEquals(AlertErrorCode.INVADED_ALERT_TIME, exception.getCode());
    }
}
