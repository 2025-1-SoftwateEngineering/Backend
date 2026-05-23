package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberWord;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.domain.member.repository.MemberWordRepository;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocaService 암기 테스트")
public class VocaServiceMemorizeTest {

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
    @DisplayName("암기 저장 성공 - 새로운 단어 저장")
    void memorizeWords_Success_NewWords() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberWordRepository.findWordIdsByMemberAndWordIn(any(), any())).willReturn(Set.of());
        given(memberWordRepository.save(any(MemberWord.class))).willReturn(null);
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of(1L, 2L));

        // when
        VocaResDTO.MemorizeInfo result = vocaService.memorizeWords(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        verify(memberWordRepository, times(2)).save(any(MemberWord.class));
    }

    @Test
    @DisplayName("암기 저장 성공 - 이미 암기한 단어는 중복 저장 안 함")
    void memorizeWords_Success_NoDuplicate() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberWordRepository.findWordIdsByMemberAndWordIn(any(), any())).willReturn(Set.of(1L));
        given(memberWordRepository.save(any(MemberWord.class))).willReturn(null);
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of(1L, 2L));

        // when
        VocaResDTO.MemorizeInfo result = vocaService.memorizeWords(1L, authMember, dto);

        // then
        assertNotNull(result);
        verify(memberWordRepository, times(1)).save(any(MemberWord.class));
    }

    @Test
    @DisplayName("암기한 단어 목록 조회 성공")
    void getMemorizedWords_Success() {
        // given
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of(1L, 2L));

        // when
        VocaResDTO.MemorizeInfo result = vocaService.getMemorizedWords(1L, authMember);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(List.of(1L, 2L), result.getMemorizedWordIds());
    }

    // ===================== 예외 케이스 =====================

    @Test
    @DisplayName("암기 저장 실패 - 존재하지 않는 단어장")
    void memorizeWords_Fail_VocaNotFound() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.memorizeWords(999L, authMember, dto));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("암기 저장 실패 - 존재하지 않는 회원")
    void memorizeWords_Fail_MemberNotFound() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.memorizeWords(1L, authMember, dto));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("암기한 단어 목록 조회 실패 - 존재하지 않는 단어장")
    void getMemorizedWords_Fail_VocaNotFound() {
        // given
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.getMemorizedWords(999L, authMember));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("암기한 단어 목록 조회 실패 - 존재하지 않는 회원")
    void getMemorizedWords_Fail_MemberNotFound() {
        // given
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.getMemorizedWords(1L, authMember));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    // ===================== 엣지 케이스 =====================

    @Test
    @DisplayName("암기 저장 - 전부 이미 암기된 단어면 save 호출 안 함")
    void memorizeWords_AllAlreadyMemorized() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberWordRepository.findWordIdsByMemberAndWordIn(any(), any())).willReturn(Set.of(1L, 2L));
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of(1L, 2L));

        // when
        VocaResDTO.MemorizeInfo result = vocaService.memorizeWords(1L, authMember, dto);

        // then
        assertNotNull(result);
        verify(memberWordRepository, times(0)).save(any(MemberWord.class));
    }

    @Test
    @DisplayName("암기 저장 - wordIds가 빈 리스트면 save 호출 안 하고 totalCount=0")
    void memorizeWords_EmptyWordIds() {
        // given
        VocaReqDTO.Memorize dto = new VocaReqDTO.Memorize();
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(wordRepository.findAllById(any())).willReturn(List.of());
        given(memberWordRepository.findWordIdsByMemberAndWordIn(any(), any())).willReturn(Set.of());
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of());

        // when
        VocaResDTO.MemorizeInfo result = vocaService.memorizeWords(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getMemorizedWordIds().isEmpty());
        verify(memberWordRepository, times(0)).save(any(MemberWord.class));
    }

    @Test
    @DisplayName("암기한 단어 목록 조회 - 암기한 단어가 없으면 빈 리스트 반환")
    void getMemorizedWords_Empty() {
        // given
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberWordRepository.findWordIdsByMemberAndVocaId(member, 1L)).willReturn(List.of());

        // when
        VocaResDTO.MemorizeInfo result = vocaService.getMemorizedWords(1L, authMember);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getMemorizedWordIds().isEmpty());
    }
}
