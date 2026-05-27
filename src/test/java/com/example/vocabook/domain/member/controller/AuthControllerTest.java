package com.example.vocabook.domain.member.controller;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.AuthResDTO;
import com.example.vocabook.domain.member.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() throws Exception {
        // given
        AuthReqDTO.SignUp request = new AuthReqDTO.SignUp("Tester", "test@example.com", "password123");
        AuthResDTO.SignUp response = new AuthResDTO.SignUp("accessToken", "refreshToken", new java.util.Date());
        
        given(authService.signUp(any(AuthReqDTO.SignUp.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("accessToken"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        AuthReqDTO.Login request = new AuthReqDTO.Login("test@example.com", "password123");
        AuthResDTO.Login response = new AuthResDTO.Login("accessToken", "refreshToken", new java.util.Date());
        
        given(authService.login(any(AuthReqDTO.Login.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("accessToken"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() throws Exception {
        // given
        AuthReqDTO.Reissue request = new AuthReqDTO.Reissue("oldRefreshToken");
        AuthResDTO.Reissue response = new AuthResDTO.Reissue("newAccessToken", "newRefreshToken", new java.util.Date());
        
        given(authService.reissue(any(AuthReqDTO.Reissue.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/v1/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("newAccessToken"));
    }
}
