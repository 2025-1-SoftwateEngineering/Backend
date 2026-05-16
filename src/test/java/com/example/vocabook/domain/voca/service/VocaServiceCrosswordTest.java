package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.member.repository.MemberCrosswordRepository;
import com.example.vocabook.domain.voca.code.VocaErrorCode;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.enums.ClueType;
import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.repository.CrosswordHintRepository;
import com.example.vocabook.domain.voca.repository.CrosswordRepository;
import com.example.vocabook.domain.voca.repository.WordRepository;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VocaServiceCrosswordTest {

    @InjectMocks
    private VocaService vocaService;

    @Mock
    private CrosswordRepository crosswordRepository;

    @Mock
    private MemberCrosswordRepository memberCrosswordRepository;

    @Mock
    private CrosswordHintRepository crosswordHintRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private RedisUtil redisUtil;

    private AuthMember authMember;
    private Member member;
    private Crossword crossword;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .coin(0L)
                .crosswordHigher(Duration.ZERO)
                .build();
        authMember = new AuthMember(member);

        crossword = Crossword.builder()
                .id(1L)
                .solvedCoin(200L)
                .build();
    }

    @Test
    @DisplayName("십자말풀이 목록 조회 성공")
    void getCrosswordListSuccess() {
        // given
        when(crosswordRepository.findCrosswordWithoutCursor(any(PageRequest.class)))
                .thenReturn(new SliceImpl<>(List.of(crossword)));
        when(memberCrosswordRepository.countByMemberAndCrossword(eq(member), eq(crossword)))
                .thenReturn(5L);

        // when
        PagingResDTO.Cursor<VocaResDTO.GetCrosswordList> result = vocaService.getCrosswordList(authMember, "-1", 10);

        // then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).id()).isEqualTo(1L);
        assertThat(result.data().get(0).solvedCoin()).isEqualTo(200L);
        assertThat(result.data().get(0).cnt()).isEqualTo(5L);
    }

    @Test
    @DisplayName("십자말풀이 문제 조회 성공")
    void getCrosswordSuccess() {
        // given
        Word mockWord = Word.builder().id(10L).englishWord("apple").meaning("사과").build();
        CrosswordHint hint = CrosswordHint.builder()
                .id(10L).crossword(crossword).word(mockWord)
                .clueType(ClueType.ACROSS)
                .clueContent("빨갛고 둥근 과일")
                .wordStartPoint("1 1") // "1 1" means verticalStartPoint=1, horizontalStartPoint=1
                .build();
        
        when(crosswordRepository.findById(crossword.getId())).thenReturn(Optional.of(crossword));
        
        MemberCrossword activeSession = MemberCrossword.builder()
                .id(100L).member(member).crossword(crossword).correctCnt(0L).solvingTime(Duration.ZERO).build();
        when(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
                .thenReturn(Optional.empty()); // No active session
        when(memberCrosswordRepository.save(any(MemberCrossword.class)))
                .thenReturn(activeSession);
        
        when(crosswordHintRepository.findAllByCrossword(crossword))
                .thenReturn(List.of(hint));

        // when
        VocaResDTO.GetCrossword result = vocaService.getCrossword(crossword.getId(), authMember);

        // then
        assertThat(result.N()).isEqualTo(5); // 0 (startPoint 1-1=0) + 5 (apple length) = 5
        assertThat(result.elements()).hasSize(1);
        assertThat(result.elements().get(0).id()).isEqualTo(10L);
        assertThat(result.elements().get(0).clueType()).isEqualTo(ClueType.ACROSS);
        assertThat(result.elements().get(0).wordLength()).isEqualTo(5);
        verify(redisUtil).save(eq("1:100"), any(LocalDateTime.class)); // Member ID: MemberCrossword ID
    }

    @Test
    @DisplayName("십자말풀이 정답 제출 성공 - 정답 (첫 문제, 아직 완료 안됨)")
    void submitCrosswordSuccess_Correct_NotFinished() {
        // given
        Word mockWord = Word.builder().id(100L).englishWord("apple").meaning("사과").build();
        CrosswordHint hint = CrosswordHint.builder()
                .id(10L).crossword(crossword).word(mockWord).build();
        MemberCrossword activeSession = MemberCrossword.builder()
                .id(100L).member(member).crossword(crossword).correctCnt(0L).solvingTime(Duration.ZERO).build();
        
        when(crosswordRepository.findById(crossword.getId())).thenReturn(Optional.of(crossword));
        when(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
                .thenReturn(Optional.of(activeSession));
        
        // Redis recorded time = 10 seconds ago
        LocalDateTime submitTime = LocalDateTime.now().minusSeconds(10);
        when(redisUtil.get("1:100")).thenReturn(submitTime);
        
        when(crosswordHintRepository.findById(10L)).thenReturn(Optional.of(hint));
        when(wordRepository.findByEnglishWord("apple")).thenReturn(Optional.of(mockWord));

        when(crosswordHintRepository.countByCrossword(crossword)).thenReturn(5L); // 5 hints total
        // Note: memberCrossword.correctCnt becomes 1 after this submit.

        // when
        VocaResDTO.SubmitCrossword result = vocaService.submitCrossword(crossword.getId(), authMember, 10L, "apple");

        // then
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.hasNext()).isTrue(); // Not all solved yet
        assertThat(result.score().getSeconds()).isGreaterThanOrEqualTo(10); // Passed 10 seconds
        assertThat(activeSession.getCorrectCnt()).isEqualTo(1L);
        verify(redisUtil).delete("1:100");
        verify(redisUtil).save(eq("1:100"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("십자말풀이 정답 제출 성공 - 전체 완료 (마지막 문제)")
    void submitCrosswordSuccess_Correct_Finished() {
        // given
        Word mockWord = Word.builder().id(100L).englishWord("apple").meaning("사과").build();
        CrosswordHint hint = CrosswordHint.builder()
                .id(10L).crossword(crossword).word(mockWord).build();
        MemberCrossword activeSession = MemberCrossword.builder()
                .id(100L).member(member).crossword(crossword).correctCnt(4L).solvingTime(Duration.ofSeconds(30)).build(); // 4 solved already
        
        when(crosswordRepository.findById(crossword.getId())).thenReturn(Optional.of(crossword));
        when(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
                .thenReturn(Optional.of(activeSession));
        
        // Redis recorded time = 5 seconds ago
        LocalDateTime submitTime = LocalDateTime.now().minusSeconds(5);
        when(redisUtil.get("1:100")).thenReturn(submitTime);
        
        when(crosswordHintRepository.findById(10L)).thenReturn(Optional.of(hint));
        when(wordRepository.findByEnglishWord("apple")).thenReturn(Optional.of(mockWord));

        when(crosswordHintRepository.countByCrossword(crossword)).thenReturn(5L); // 5 hints total (this submit makes it 5)
        when(memberCrosswordRepository.countByMemberAndCrossword(member, crossword)).thenReturn(1L); // First clear

        // when
        VocaResDTO.SubmitCrossword result = vocaService.submitCrossword(crossword.getId(), authMember, 10L, "apple");

        // then
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.hasNext()).isFalse(); // All solved
        assertThat(activeSession.getSolvedAt()).isNotNull(); // End called
        assertThat(member.getCoin()).isEqualTo(200L); // Reward paid
        assertThat(member.getCrosswordHigher().getSeconds()).isGreaterThanOrEqualTo(35); // Initial 30s + 5s = 35s
    }

    @Test
    @DisplayName("십자말풀이 정답 제출 실패 - 오답")
    void submitCrosswordSuccess_Incorrect() {
        // given
        Word correctWord = Word.builder().id(100L).englishWord("apple").meaning("사과").build();
        Word incorrectWord = Word.builder().id(101L).englishWord("banana").meaning("바나나").build();
        CrosswordHint hint = CrosswordHint.builder()
                .id(10L).crossword(crossword).word(correctWord).build();
        MemberCrossword activeSession = MemberCrossword.builder()
                .id(100L).member(member).crossword(crossword).correctCnt(0L).solvingTime(Duration.ZERO).build();
        
        when(crosswordRepository.findById(crossword.getId())).thenReturn(Optional.of(crossword));
        when(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
                .thenReturn(Optional.of(activeSession));
        
        LocalDateTime submitTime = LocalDateTime.now().minusSeconds(5);
        when(redisUtil.get("1:100")).thenReturn(submitTime);
        
        when(crosswordHintRepository.findById(10L)).thenReturn(Optional.of(hint));
        when(wordRepository.findByEnglishWord("banana")).thenReturn(Optional.of(incorrectWord)); // User typed 'banana'
        when(crosswordHintRepository.countByCrossword(crossword)).thenReturn(5L);
        when(memberCrosswordRepository.countByMemberAndCrossword(member, crossword)).thenReturn(1L);

        // when
        VocaResDTO.SubmitCrossword result = vocaService.submitCrossword(crossword.getId(), authMember, 10L, "banana");

        // then
        assertThat(result.isCorrect()).isFalse();
        assertThat(result.hasNext()).isTrue();
        assertThat(activeSession.getCorrectCnt()).isEqualTo(0L); // Not increased
        verify(redisUtil, never()).delete("1:100"); // Time not reset
    }

    @Test
    @DisplayName("십자말풀이 정답 제출 실패 - 존재하지 않는 단어")
    void submitCrosswordException_WordNotFound() {
        // given
        CrosswordHint hint = CrosswordHint.builder().id(10L).crossword(crossword).build();
        MemberCrossword activeSession = MemberCrossword.builder()
                .id(100L).member(member).crossword(crossword).correctCnt(0L).solvingTime(Duration.ZERO).build();
        
        when(crosswordRepository.findById(crossword.getId())).thenReturn(Optional.of(crossword));
        when(memberCrosswordRepository.findByMemberAndCrosswordAndSolvedAtIsNull(member, crossword))
                .thenReturn(Optional.of(activeSession));
        when(redisUtil.get("1:100")).thenReturn(LocalDateTime.now());
        when(crosswordHintRepository.findById(10L)).thenReturn(Optional.of(hint));
        when(wordRepository.findByEnglishWord("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vocaService.submitCrossword(crossword.getId(), authMember, 10L, "unknown"))
                .isInstanceOf(VocaException.class)
                .extracting("code").isEqualTo(VocaErrorCode.WORD_NOT_FOUND);
    }
}
