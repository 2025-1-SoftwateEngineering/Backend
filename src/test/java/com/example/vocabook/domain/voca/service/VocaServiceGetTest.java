package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.domain.member.repository.MemberWordRepository;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.code.VocaErrorCode;
import com.example.vocabook.domain.voca.repository.VocaRepository;
import com.example.vocabook.domain.voca.repository.WordRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocaService 조회 테스트")
public class VocaServiceGetTest {

    @Mock private VocaRepository vocaRepository;
    @Mock private WordRepository wordRepository;
    @Mock private MemberVocaRepository memberVocaRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberWordRepository memberWordRepository;

    @InjectMocks
    private VocaService vocaService;

    private Member member;
    private AuthMember authMember;
    private Voca voca;
    private Word word1, word2;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스터")
                .password("password")
                .refreshToken("token")
                .build();
        authMember = new AuthMember(member);

        voca = Voca.builder()
                .id(1L)
                .description("TOEIC 핵심 단어")
                .createdAt(LocalDateTime.now())
                .build();

        word1 = Word.builder().id(1L).englishWord("apple").meaning("사과").voca(voca).build();
        word2 = Word.builder().id(2L).englishWord("banana").meaning("바나나").voca(voca).build();
    }

    @Test
    @DisplayName("단어장 목록 조회 성공")
    void getVocaList_Success() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(vocaRepository.findAll()).willReturn(List.of(voca));
        given(wordRepository.findByVocaIn(List.of(voca))).willReturn(List.of(word1, word2));
        given(memberWordRepository.countByMemberGroupByVoca(any(), any())).willReturn(List.of());

        // when
        VocaResDTO.VocaList result = vocaService.getVocaList(authMember);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(2, result.getVocas().get(0).getWordCount());
    }

    @Test
    @DisplayName("단어 목록 조회 성공")
    void getWords_Success() {
        // given
        Page<Word> wordPage = new PageImpl<>(List.of(word1, word2), PageRequest.of(0, 10), 2);
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findByVoca(any(), any())).willReturn(wordPage);

        // when
        VocaResDTO.WordList result = vocaService.getWords(1L, 0, 10);

        // then
        assertNotNull(result);
        assertEquals(2, result.getWords().size());
        assertEquals("TOEIC 핵심 단어", result.getDescription());
        assertEquals(1L, result.getVocaId());
    }

    @Test
    @DisplayName("단어 테스트 문제 조회 성공")
    void getTestQuestions_Success() {
        // given
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findByVoca(voca)).willReturn(List.of(word1, word2));

        // when
        List<VocaResDTO.TestQuestion> result = vocaService.getTestQuestions(1L);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("학습한 단어장 목록 조회 성공")
    void getStudiedVocas_Success() {
        // given
        MemberVoca memberVoca = MemberVoca.builder()
                .member(member)
                .voca(voca)
                .correctCnt(8L)
                .learningWordCnt(10L)
                .solvedAt(LocalDateTime.now())
                .build();
        given(memberVocaRepository.findAllByMember(member)).willReturn(List.of(memberVoca));

        // when
        VocaResDTO.StudiedVocaList result = vocaService.getStudiedVocas(authMember);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(8L, result.getVocas().get(0).getCorrectCnt());
    }

    // ===================== 예외 케이스 =====================

    @Test
    @DisplayName("단어장 목록 조회 실패 - 존재하지 않는 회원")
    void getVocaList_Fail_MemberNotFound() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.getVocaList(authMember));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("단어 목록 조회 실패 - 존재하지 않는 단어장")
    void getWords_Fail_VocaNotFound() {
        // given
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.getWords(999L, 0, 10));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("테스트 문제 조회 실패 - 존재하지 않는 단어장")
    void getTestQuestions_Fail_VocaNotFound() {
        // given
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.getTestQuestions(999L));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    // ===================== 엣지 케이스 =====================

    @Test
    @DisplayName("단어장 목록 조회 - 단어장이 없으면 빈 리스트 반환")
    void getVocaList_EmptyVocaList() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(vocaRepository.findAll()).willReturn(List.of());
        given(wordRepository.findByVocaIn(List.of())).willReturn(List.of());
        given(memberWordRepository.countByMemberGroupByVoca(any(), any())).willReturn(List.of());

        // when
        VocaResDTO.VocaList result = vocaService.getVocaList(authMember);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getVocas().isEmpty());
    }

    @Test
    @DisplayName("학습한 단어장 목록 조회 - 학습 기록 없으면 빈 리스트 반환")
    void getStudiedVocas_Empty() {
        // given
        given(memberVocaRepository.findAllByMember(member)).willReturn(List.of());

        // when
        VocaResDTO.StudiedVocaList result = vocaService.getStudiedVocas(authMember);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getVocas().isEmpty());
    }

    @Test
    @DisplayName("단어 테스트 문제 조회 - 단어가 없으면 빈 리스트 반환")
    void getTestQuestions_EmptyWords() {
        // given
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findByVoca(voca)).willReturn(List.of());

        // when
        List<VocaResDTO.TestQuestion> result = vocaService.getTestQuestions(1L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("단어 목록 조회 - 단어가 없으면 빈 페이지 반환")
    void getWords_EmptyPage() {
        // given
        Page<Word> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findByVoca(any(), any())).willReturn(emptyPage);

        // when
        VocaResDTO.WordList result = vocaService.getWords(1L, 0, 10);

        // then
        assertNotNull(result);
        assertTrue(result.getWords().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
