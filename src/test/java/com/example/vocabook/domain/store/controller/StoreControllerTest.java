package com.example.vocabook.domain.store.controller;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.store.dto.StoreReqDTO;
import com.example.vocabook.domain.store.dto.StoreResDTO;
import com.example.vocabook.domain.store.service.StoreService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreController storeController;

    private AuthMember authMember;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .nickname("Tester")
                .build();
        authMember = new AuthMember(member);

        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(AuthMember.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return authMember;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("상점 아이템 목록 조회 성공")
    void getItemList_Success() throws Exception {
        // given
        StoreResDTO.ItemList response = StoreResDTO.ItemList.builder()
                .items(Collections.emptyList())
                .build();
        given(storeService.getItemList()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/store/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("아이템 구매 성공")
    void purchaseItem_Success() throws Exception {
        // given
        StoreResDTO.PurchaseResult response = StoreResDTO.PurchaseResult.builder()
                .purchasedItem(StoreResDTO.ItemInfo.builder().itemId(1L).build())
                .remainingCoins(100L)
                .build();
        given(storeService.purchaseItem(eq(1L), any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/store/items/1/purchase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.purchasedItem.itemId").value(1L))
                .andExpect(jsonPath("$.result.remainingCoins").value(100L));
    }

    @Test
    @DisplayName("내 아이템 목록 조회 성공")
    void getMyItems_Success() throws Exception {
        // given
        StoreResDTO.MyItemList response = StoreResDTO.MyItemList.builder()
                .items(Collections.emptyList())
                .build();
        given(storeService.getMyItems(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/store/my-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("아이템 사용 성공 - request 바디 포함")
    void useItem_Success_WithRequestBody() throws Exception {
        // given
        StoreResDTO.UseResult response = StoreResDTO.UseResult.builder()
                .itemName("item")
                .remainingCount(1L)
                .build();
        StoreReqDTO.UseItemRequest request = new StoreReqDTO.UseItemRequest(2L);
        
        given(storeService.useItem(eq(1L), any(AuthMember.class), eq(2L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/store/items/1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.itemName").value("item"));
    }

    @Test
    @DisplayName("아이템 사용 성공 - request 바디 미포함")
    void useItem_Success_WithoutRequestBody() throws Exception {
        // given
        StoreResDTO.UseResult response = StoreResDTO.UseResult.builder()
                .itemName("item")
                .remainingCount(1L)
                .build();
        
        given(storeService.useItem(eq(1L), any(AuthMember.class), eq(null))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/store/items/1/use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.itemName").value("item"));
    }
}
