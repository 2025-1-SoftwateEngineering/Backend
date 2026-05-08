package com.example.vocabook.domain.alert.controller;

import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.service.AlertService;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController API 단위 테스트")
public class AlertControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // OffsetDateTime 등의 파싱을 위해 필요

        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        authMember = new AuthMember(member);

        // Standalone Setup (Security 필터 없이 Controller 레이어만 고립 테스트)
        mockMvc = MockMvcBuilders.standaloneSetup(alertController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(AuthMember.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return authMember; // @AuthenticationPrincipal AuthMember auth에 mock 객체 주입
                    }
                })
                .build();
    }

    @Test
    @DisplayName("FCM 토큰 등록 API 성공")
    void registerFcm_Success() throws Exception {
        // given
        AlertReqDTO.RegisterFcm request = new AlertReqDTO.RegisterFcm("test-token");
        AlertResDTO.RegisterFcm response = AlertResDTO.RegisterFcm.builder()
                .memberId(1L)
                .registeredAt(LocalDateTime.now())
                .build();

        given(alertService.registerFcm(any(AuthMember.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/fcm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberId").value(1L));
    }

    @Test
    @DisplayName("알림 커스텀 API 성공")
    void customAlert_Success() throws Exception {
        // given
        OffsetDateTime alertedAt = OffsetDateTime.now().plusDays(1);
        AlertReqDTO.CustomAlert request = new AlertReqDTO.CustomAlert("test message", Repeat.NONE, alertedAt);
        AlertResDTO.CustomAlert response = AlertResDTO.CustomAlert.builder()
                .message("test message")
                .repeat(Repeat.NONE)
                .alertedAt(alertedAt.toLocalDateTime())
                .build();

        given(alertService.customAlert(any(AuthMember.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/alerts/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.message").value("test message"))
                .andExpect(jsonPath("$.result.repeat").value("NONE"));
    }

    @Test
    @DisplayName("알림 리스트 조회 API 성공")
    void getAlertList_Success() throws Exception {
        // given
        AlertResDTO.AlertList alertListItem = AlertResDTO.AlertList.builder()
                .alertId(1L)
                .message("test")
                .repeat(Repeat.NONE)
                .alertedAt(LocalDateTime.now())
                .build();
                
        PagingResDTO.Cursor<AlertResDTO.AlertList> response = PagingResDTO.Cursor.<AlertResDTO.AlertList>builder()
                .data(List.of(alertListItem))
                .nextCursor("1")
                .hasNext(false)
                .pageSize(1)
                .build();

        given(alertService.getAlertList(any(AuthMember.class), eq("-1"), eq(10))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/alerts")
                        .param("cursor", "-1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.nextCursor").value("1"));
    }

    @Test
    @DisplayName("알림 삭제 API 성공")
    void deleteAlert_Success() throws Exception {
        // given
        AlertResDTO.DeleteAlert response = AlertResDTO.DeleteAlert.builder()
                .alertId(1L)
                .deletedAt(LocalDateTime.now())
                .build();

        given(alertService.deleteAlert(any(AuthMember.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/v1/alerts/{alertId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.id").value(1L));
    }
}
