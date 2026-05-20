package com.example.vocabook.domain.voca.controller;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.service.VocaService;
import com.example.vocabook.global.security.entity.AuthMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {VocaControllerTest.TestMvcConfig.class})
@WebAppConfiguration
@DisplayName("VocaController API 단위 테스트")
public class VocaControllerTest {

    private static AuthMember authMember;

    @Configuration
    @EnableWebMvc
    static class TestMvcConfig implements WebMvcConfigurer {

        @Bean
        public VocaService vocaService() {
            return Mockito.mock(VocaService.class);
        }

        @Bean
        public VocaController vocaController(VocaService vocaService) {
            return new VocaController(vocaService);
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().isAssignableFrom(AuthMember.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    return authMember;
                }
            });
        }
    }

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private VocaService vocaService;

    @Autowired(required = false)
    private RequestMappingHandlerMapping handlerMapping;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // CI 디버그: 등록된 핸들러 매핑 출력
        if (handlerMapping != null) {
            System.out.println("[DEBUG] Registered handler mappings: " + handlerMapping.getHandlerMethods().keySet());
        } else {
            System.out.println("[DEBUG] handlerMapping is NULL");
        }
        System.out.println("[DEBUG] wac class: " + wac.getClass().getName());
        System.out.println("[DEBUG] wac beans: " + java.util.Arrays.toString(wac.getBeanDefinitionNames()));
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스터")
                .password("password")
                .refreshToken("token")
                .build();
        authMember = new AuthMember(member);

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        Mockito.reset(vocaService);
    }

    @Test
    @DisplayName("단어장 목록 조회 API 성공")
    void getVocaList_Success() throws Exception {
        // given
        VocaResDTO.VocaInfo vocaInfo = VocaResDTO.VocaInfo.builder()
                .vocaId(1L).description("TOEIC 핵심 단어").wordCount(20).memorizedCount(5)
                .createdAt(LocalDateTime.now()).build();
        VocaResDTO.VocaList response = VocaResDTO.VocaList.builder()
                .vocas(List.of(vocaInfo)).totalCount(1).build();

        given(vocaService.getVocaList(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(1))
                .andExpect(jsonPath("$.result.vocas[0].description").value("TOEIC 핵심 단어"));
    }

    @Test
    @DisplayName("단어 목록 조회 API 성공")
    void getWords_Success() throws Exception {
        // given
        VocaResDTO.WordInfo wordInfo = VocaResDTO.WordInfo.builder()
                .wordId(1L).englishWord("apple").meaning("사과").build();
        VocaResDTO.WordList response = VocaResDTO.WordList.builder()
                .vocaId(1L).description("TOEIC 핵심 단어")
                .words(List.of(wordInfo)).totalPages(1).totalElements(1).build();

        given(vocaService.getWords(eq(1L), eq(0), eq(10))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/voca/1/words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.description").value("TOEIC 핵심 단어"))
                .andExpect(jsonPath("$.result.words[0].englishWord").value("apple"));
    }

    @Test
    @DisplayName("단어 테스트 문제 조회 API 성공")
    void getTestQuestions_Success() throws Exception {
        // given
        VocaResDTO.TestQuestion question = VocaResDTO.TestQuestion.builder()
                .wordId(1L).englishWord("apple").meaning("사과").build();

        given(vocaService.getTestQuestions(1L)).willReturn(List.of(question));

        // when & then
        mockMvc.perform(get("/api/v1/voca/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result[0].meaning").value("사과"));
    }

    @Test
    @DisplayName("단어 즉시 채점 API 성공 - 정답")
    void submitAnswer_Success() throws Exception {
        // given
        VocaResDTO.AnswerResult response = VocaResDTO.AnswerResult.builder()
                .wordId(1L).meaning("사과").correctAnswer("apple").submittedAnswer("apple").correct(true).build();

        given(vocaService.submitAnswer(eq(1L), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/voca/1/test/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wordId\":1,\"answer\":\"apple\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.isCorrect").value(true));
    }

    @Test
    @DisplayName("암기한 단어 목록 조회 API 성공")
    void getMemorizedWords_Success() throws Exception {
        // given
        VocaResDTO.MemorizeInfo response = VocaResDTO.MemorizeInfo.builder()
                .memorizedWordIds(List.of(1L, 2L)).totalCount(2).build();

        given(vocaService.getMemorizedWords(eq(1L), any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/1/memorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(2))
                .andExpect(jsonPath("$.result.memorizedWordIds[0]").value(1))
                .andExpect(jsonPath("$.result.memorizedWordIds[1]").value(2));
    }

    @Test
    @DisplayName("암기 저장 API 성공")
    void memorizeWords_Success() throws Exception {
        // given
        VocaResDTO.MemorizeInfo response = VocaResDTO.MemorizeInfo.builder()
                .memorizedWordIds(List.of(1L, 2L)).totalCount(2).build();

        given(vocaService.memorizeWords(eq(1L), any(AuthMember.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/1/memorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wordIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(2));
    }

    @Test
    @DisplayName("학습한 단어장 목록 조회 API 성공")
    void getStudiedVocas_Success() throws Exception {
        // given
        VocaResDTO.StudiedVocaInfo vocaInfo = VocaResDTO.StudiedVocaInfo.builder()
                .vocaId(1L).description("TOEIC 핵심 단어").learningWordCnt(10L).correctCnt(8L)
                .solvedAt(LocalDateTime.now()).build();
        VocaResDTO.StudiedVocaList response = VocaResDTO.StudiedVocaList.builder()
                .vocas(List.of(vocaInfo)).totalCount(1).build();

        given(vocaService.getStudiedVocas(any(AuthMember.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(1));
    }

    @Test
    @DisplayName("테스트 완료 API 성공")
    void completeTest_Success() throws Exception {
        // given
        VocaResDTO.TestResult response = VocaResDTO.TestResult.builder()
                .totalCount(2).correctCount(2).wrongCount(0).earnedCoins(10L)
                .results(List.of()).build();

        given(vocaService.completeTest(eq(1L), any(AuthMember.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/1/test/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"wordId\":1,\"answer\":\"apple\"},{\"wordId\":2,\"answer\":\"banana\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.correctCount").value(2))
                .andExpect(jsonPath("$.result.earnedCoins").value(10));
    }

    // ===================== 4xx 케이스 =====================

    @Test
    @DisplayName("즉시 채점 API - body 없이 요청하면 400")
    void submitAnswer_NoBody_BadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/voca/1/test/submit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("테스트 완료 API - body 없이 요청하면 400")
    void completeTest_NoBody_BadRequest() throws Exception {
        mockMvc.perform(post("/api/1/test/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("암기 저장 API - body 없이 요청하면 400")
    void memorize_NoBody_BadRequest() throws Exception {
        mockMvc.perform(post("/api/1/memorize")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("단어 목록 조회 API - vocaId에 문자열이 오면 400")
    void getWords_InvalidVocaId_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/voca/abc/words"))
                .andExpect(status().isBadRequest());
    }
}
