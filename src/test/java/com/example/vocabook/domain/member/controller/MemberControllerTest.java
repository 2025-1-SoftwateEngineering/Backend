package com.example.vocabook.domain.member.controller;

import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.service.MemberService;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .nickname("Tester")
                .authorize(Authorize.ROLE_USER)
                .loginAt(LocalDateTime.now())
                .streak(0L)
                .coin(0L)
                .refreshToken("token")
                .build();
        authMember = new AuthMember(member);

        // Standalone Setup
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
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
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        // given
        MemberResDTO.MyProfile response = new MemberResDTO.MyProfile("Tester", "test@example.com", 0L, 0L);
        given(memberService.getMyProfile(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.nickname").value("Tester"))
                .andExpect(jsonPath("$.result.email").value("test@example.com"));
    }

    @Test
    @DisplayName("친구 요청 목록 조회 성공")
    void getFriendRequestList_Success() throws Exception {
        // given
        PagingResDTO.Cursor<MemberResDTO.FriendRequestList> response = PagingResDTO.Cursor.<MemberResDTO.FriendRequestList>builder()
                .data(List.of())
                .hasNext(false)
                .nextCursor("-1")
                .build();
        given(memberService.getFriendRequestList(any(), any(), any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/friends/request")
                        .param("cursor", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("친구 요청 보내기 성공")
    void sendFriendRequest_Success() throws Exception {
        // given
        Long friendId = 2L;
        MemberResDTO.SendFriendRequest response = new MemberResDTO.SendFriendRequest(1L, "Tester");
        given(memberService.sendFriendRequest(any(AuthMember.class), eq(friendId))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/friends/{friendId}/request", friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.nickname").value("Tester"));
    }

    @Test
    @DisplayName("친구 요청 수락 토글 성공")
    void updateFriendRequest_Success() throws Exception {
        // given
        Long fromMemberId = 2L;
        MemberReqDTO.UpdateFriendRequest request = new MemberReqDTO.UpdateFriendRequest("ACCEPTED");
        MemberResDTO.UpdateFriendRequest response = new MemberResDTO.UpdateFriendRequest(1L, "Friend", FriendState.valueOf("ACCEPTED"));
        
        given(memberService.updateFriendRequest(any(AuthMember.class), eq(fromMemberId), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/friends/{friendId}", fromMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("사용자 단순 조회 성공")
    void searchMember_Success() throws Exception {
        // given
        MemberReqDTO.SearchMember request = new MemberReqDTO.SearchMember("friend@example.com");
        MemberResDTO.SearchMember response = new MemberResDTO.SearchMember(2L, "Friend", "friend@example.com");
        
        given(memberService.searchMember(any(AuthMember.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/members/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.id").value(2L));
    }

    @Test
    @DisplayName("친구 목록 조회 성공")
    void getFriendList_Success() throws Exception {
        // given
        PagingResDTO.Cursor<MemberResDTO.FriendList> response = PagingResDTO.Cursor.<MemberResDTO.FriendList>builder()
                .data(List.of())
                .hasNext(false)
                .nextCursor("-1")
                .build();
        given(memberService.getFriendList(any(AuthMember.class), any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/friends")
                        .param("cursor", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("친구 프로필 조회 성공")
    void getFriendProfile_Success() throws Exception {
        // given
        Long friendId = 2L;
        MemberResDTO.FriendProfile response = new MemberResDTO.FriendProfile(2L, "Friend", 0L, 0L, LocalDateTime.now());
        given(memberService.getFriendProfile(any(AuthMember.class), eq(friendId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/friends/{friendId}", friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.nickname").value("Friend"));
    }

    @Test
    @DisplayName("친구 차단 성공")
    void blockMember_Success() throws Exception {
        // given
        Long friendId = 2L;
        MemberResDTO.Blocking response = new MemberResDTO.Blocking(1L, "Friend", LocalDateTime.now());
        given(memberService.blockMember(any(AuthMember.class), eq(friendId))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/friends/{friendId}/block", friendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }
}
