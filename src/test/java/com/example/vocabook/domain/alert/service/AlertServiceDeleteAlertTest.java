package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService 알림 삭제 테스트")
public class AlertServiceDeleteAlertTest {

    @Mock
    private AlertDetailRepository alertDetailRepository;

    @Mock
    private AlertScheduleService alertScheduleService;

    @InjectMocks
    private AlertService alertService;

    private Member myMember;
    private AuthMember authMember;
    private Alert myAlert;
    private AlertDetail myAlertDetail;

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

        myAlertDetail = AlertDetail.builder()
                .id(1L)
                .content("test message")
                .repeat(Repeat.NONE)
                .alertedAt(LocalDateTime.now())
                .alert(myAlert)
                .build();
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteAlert_Success() throws SchedulerException {
        // given
        given(alertDetailRepository.findById(eq(1L))).willReturn(Optional.of(myAlertDetail));

        // when
        AlertResDTO.DeleteAlert result = alertService.deleteAlert(authMember, 1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.alertId());
        verify(alertDetailRepository).delete(myAlertDetail);
        verify(alertScheduleService).deleteAlarm(1L);
    }

    @Test
    @DisplayName("알림 삭제 실패 - 스케줄러 삭제 오류 (FAILED_DELETE_ALERT)")
    void deleteAlert_Fail_SchedulerError() throws SchedulerException {
        // given
        given(alertDetailRepository.findById(eq(1L))).willReturn(Optional.of(myAlertDetail));
        willThrow(new SchedulerException("quartz error")).given(alertScheduleService).deleteAlarm(1L);

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.deleteAlert(authMember, 1L)
        );

        // then
        assertEquals(AlertErrorCode.FAILED_DELETE_ALERT, exception.getCode());
    }

    @Test
    @DisplayName("알림 삭제 실패 - 존재하지 않는 알림 (NOT_FOUND)")
    void deleteAlert_Fail_NotFound() {
        // given
        given(alertDetailRepository.findById(eq(2L))).willReturn(Optional.empty());

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.deleteAlert(authMember, 2L)
        );

        // then
        assertEquals(AlertErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("알림 삭제 실패 - 권한 없음 (MISMATCH_USER)")
    void deleteAlert_Fail_MismatchUser() {
        // given
        Member otherMember = Member.builder().id(2L).email("other@example.com").build();
        Alert otherAlert = Alert.builder().fcmToken("other-token").member(otherMember).build();
        AlertDetail otherAlertDetail = AlertDetail.builder()
                .id(1L)
                .content("test message")
                .repeat(Repeat.NONE)
                .alertedAt(LocalDateTime.now())
                .alert(otherAlert)
                .build();

        given(alertDetailRepository.findById(eq(1L))).willReturn(Optional.of(otherAlertDetail));

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.deleteAlert(authMember, 1L)
        );

        // then
        assertEquals(AlertErrorCode.MISMATCH_USER, exception.getCode());
    }
}
