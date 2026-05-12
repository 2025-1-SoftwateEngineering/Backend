package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService 알림 리스트 조회 테스트")
public class AlertServiceGetAlertListTest {

    @Mock
    private AlertDetailRepository alertDetailRepository;

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
    @DisplayName("알림 리스트 조회 성공 - 커서 없이 조회")
    void getAlertList_Success_WithoutCursor() {
        // given
        Slice<AlertDetail> slice = new SliceImpl<>(List.of(myAlertDetail), PageRequest.ofSize(10), false);
        given(alertDetailRepository.findAlertListWithoutCursor(eq(myMember), any(PageRequest.class))).willReturn(slice);

        // when
        PagingResDTO.Cursor<AlertResDTO.AlertList> result = alertService.getAlertList(authMember, "-1", 10);

        // then
        assertNotNull(result);
        assertFalse(result.hasNext());
        assertEquals("1", result.nextCursor());
        assertEquals(1, result.data().size());
        assertEquals("test message", result.data().getFirst().message());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 커서 포함 조회")
    void getAlertList_Success_WithCursor() {
        // given
        Slice<AlertDetail> slice = new SliceImpl<>(List.of(myAlertDetail), PageRequest.ofSize(10), false);
        given(alertDetailRepository.findAlertListWithCursor(eq(myMember), eq(2L), any(PageRequest.class))).willReturn(slice);

        // when
        PagingResDTO.Cursor<AlertResDTO.AlertList> result = alertService.getAlertList(authMember, "2", 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.data().size());
    }

    @Test
    @DisplayName("알림 리스트 조회 성공 - 데이터가 없을 때")
    void getAlertList_Success_EmptyList() {
        // given
        Slice<AlertDetail> emptySlice = new SliceImpl<>(List.of(), PageRequest.ofSize(10), false);
        given(alertDetailRepository.findAlertListWithoutCursor(eq(myMember), any(PageRequest.class))).willReturn(emptySlice);

        // when
        PagingResDTO.Cursor<AlertResDTO.AlertList> result = alertService.getAlertList(authMember, "-1", 10);

        // then
        assertNotNull(result);
        assertNull(result.data());
        assertFalse(result.hasNext());
    }

    @Test
    @DisplayName("알림 리스트 조회 실패 - 잘못된 커서 형식 (INVADED_CURSOR)")
    void getAlertList_Fail_InvadedCursor() {
        // given
        String invalidCursor = "abc";

        // when
        AlertException exception = assertThrows(AlertException.class, () ->
                alertService.getAlertList(authMember, invalidCursor, 10)
        );

        // then
        assertEquals(GeneralErrorCode.INVADED_CURSOR, exception.getCode());
    }
}
