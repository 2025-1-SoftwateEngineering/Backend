package com.example.vocabook.domain.pet.controller;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.pet.dto.PetResDTO;
import com.example.vocabook.domain.pet.enums.PetStage;
import com.example.vocabook.domain.pet.service.PetService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

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

        mockMvc = MockMvcBuilders.standaloneSetup(petController)
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
    @DisplayName("내 펫 조회 성공")
    void getPet_Success() throws Exception {
        // given
        PetResDTO.PetInfo response = PetResDTO.PetInfo.builder()
                .petId(1L)
                .stage(PetStage.EGG)
                .petImageUrl("egg.png")
                .activeBackgroundUrl("bg.png")
                .activeAccessoryUrl("acc.png")
                .build();
        given(petService.getPet(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/pets/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.petId").value(1L))
                .andExpect(jsonPath("$.result.stage").value("EGG"));
    }

    @Test
    @DisplayName("펫 생성 성공")
    void createPet_Success() throws Exception {
        // given
        PetResDTO.PetInfo response = PetResDTO.PetInfo.builder()
                .petId(2L)
                .stage(PetStage.EGG)
                .petImageUrl("egg.png")
                .activeBackgroundUrl("bg.png")
                .activeAccessoryUrl("acc.png")
                .build();
        given(petService.createPet(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.petId").value(2L))
                .andExpect(jsonPath("$.result.stage").value("EGG"));
    }
}
