package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.domain.member.repository.MemberWordRepository;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
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

import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.code.VocaErrorCode;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocaService 테스트 채점 테스트")
public class VocaServiceCompleteTestTest {

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
    @DisplayName("단어 즉시 채점 성공 - 정답")
    void submitAnswer_Correct() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 1L);
        setField(dto, "answer", "apple");

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findById(1L)).willReturn(Optional.of(word1));

        // when
        VocaResDTO.AnswerResult result = vocaService.submitAnswer(1L, dto);

        // then
        assertNotNull(result);
        assertTrue(result.isCorrect());
        assertEquals("apple", result.getCorrectAnswer());
    }

    @Test
    @DisplayName("단어 즉시 채점 성공 - 오답")
    void submitAnswer_Wrong() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 1L);
        setField(dto, "answer", "orange");

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findById(1L)).willReturn(Optional.of(word1));

        // when
        VocaResDTO.AnswerResult result = vocaService.submitAnswer(1L, dto);

        // then
        assertNotNull(result);
        assertFalse(result.isCorrect());
    }

    @Test
    @DisplayName("테스트 완료 성공 - 당일 첫 제출 (코인 5배 + 스트릭 증가)")
    void completeTest_Success_FirstSubmit() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(
                makeAnswer(1L, "apple"),
                makeAnswer(2L, "banana")
        ));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberRepository.saveAndFlush(any())).willReturn(member);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(2, result.getCorrectCount());
        assertEquals(10L, result.getEarnedCoins()); // 2 * 5
        assertEquals(0, result.getWrongCount());
    }

    @Test
    @DisplayName("테스트 완료 성공 - 당일 재제출, 이전과 동일한 정답 (코인 지급 없음)")
    void completeTest_Success_ReSubmit_SameCorrect() {
        // given - 이전에 apple 1개 맞춤(correctCnt=1), 이번에도 apple만 맞춤
        MemberVoca existing = MemberVoca.builder()
                .member(member).voca(voca)
                .correctCnt(1L).learningWordCnt(2L)
                .solvedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(
                makeAnswer(1L, "apple"),
                makeAnswer(2L, "wrong")
        ));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.of(existing));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberRepository.saveAndFlush(any())).willReturn(member);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCorrectCount());
        assertEquals(0L, result.getEarnedCoins()); // 이전에 이미 맞춘 단어 → 코인 없음
        assertEquals(1, result.getWrongCount());
    }

    @Test
    @DisplayName("테스트 완료 성공 - 당일 재제출, 새로 맞춘 단어 있으면 5코인")
    void completeTest_Success_ReSubmit_NewlyCorrect() {
        // given - 이전에 0개 맞춤(correctCnt=0), 이번에 apple 맞춤
        MemberVoca existing = MemberVoca.builder()
                .member(member).voca(voca)
                .correctCnt(0L).learningWordCnt(2L)
                .solvedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(
                makeAnswer(1L, "apple"),
                makeAnswer(2L, "wrong")
        ));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.of(existing));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberRepository.saveAndFlush(any())).willReturn(member);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCorrectCount());
        assertEquals(5L, result.getEarnedCoins()); // 새로 맞춘 1개 * 5코인
        assertEquals(1, result.getWrongCount());
    }

    @Test
    @DisplayName("단어 즉시 채점 - 대소문자 구분 없이 정답 처리")
    void submitAnswer_CaseInsensitive() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 1L);
        setField(dto, "answer", "APPLE");

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findById(1L)).willReturn(Optional.of(word1));

        // when
        VocaResDTO.AnswerResult result = vocaService.submitAnswer(1L, dto);

        // then
        assertTrue(result.isCorrect());
    }

    // ===================== 예외 케이스 =====================

    @Test
    @DisplayName("즉시 채점 실패 - 존재하지 않는 단어장")
    void submitAnswer_Fail_VocaNotFound() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 1L);
        setField(dto, "answer", "apple");
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.submitAnswer(999L, dto));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("즉시 채점 실패 - 존재하지 않는 단어")
    void submitAnswer_Fail_WordNotFound() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 999L);
        setField(dto, "answer", "apple");
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.submitAnswer(1L, dto));
        assertEquals(VocaErrorCode.WORD_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("테스트 완료 실패 - 존재하지 않는 단어장")
    void completeTest_Fail_VocaNotFound() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of());
        given(vocaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.completeTest(999L, authMember, dto));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("테스트 완료 실패 - 존재하지 않는 회원")
    void completeTest_Fail_MemberNotFound() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of());
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.completeTest(1L, authMember, dto));
        assertEquals(VocaErrorCode.VOCA_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("테스트 완료 실패 - 답안 속 존재하지 않는 단어")
    void completeTest_Fail_WordNotFound() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(999L, "apple")));
        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of()); // 999L은 DB에 없음

        // when & then
        VocaException exception = assertThrows(VocaException.class,
                () -> vocaService.completeTest(1L, authMember, dto));
        assertEquals(VocaErrorCode.WORD_NOT_FOUND, exception.getCode());
    }

    // ===================== 엣지 케이스 =====================

    @Test
    @DisplayName("즉시 채점 - 공백 포함 답안은 정답 처리")
    void submitAnswer_WithTrailingSpace_Correct() {
        // given
        VocaReqDTO.SubmitAnswer dto = new VocaReqDTO.SubmitAnswer();
        setField(dto, "wordId", 1L);
        setField(dto, "answer", "apple ");

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(wordRepository.findById(1L)).willReturn(Optional.of(word1));

        // when
        VocaResDTO.AnswerResult result = vocaService.submitAnswer(1L, dto);

        // then
        assertTrue(result.isCorrect());
    }

    @Test
    @DisplayName("테스트 완료 - streak 6에서 7이 되면 보너스 500코인 지급")
    void completeTest_StreakBonus() {
        // given
        Member memberWithStreak6 = Member.builder()
                .id(1L).email("test@example.com").nickname("테스터")
                .password("password").refreshToken("token").streak(6L).build();

        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(1L, "apple")));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(memberWithStreak6));
        given(memberVocaRepository.findByMemberAndVoca(memberWithStreak6, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of(word1));
        given(memberRepository.saveAndFlush(any())).willReturn(memberWithStreak6);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(505L, result.getEarnedCoins()); // 정답 1개 * 5코인 + 보너스 500코인
    }

    @Test
    @DisplayName("테스트 완료 - 전부 오답이어도 첫 제출이면 streak +1")
    void completeTest_AllWrong_FirstSubmit_StreakIncreases() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(
                makeAnswer(1L, "wrong1"),
                makeAnswer(2L, "wrong2")
        ));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of(word1, word2));
        given(memberRepository.saveAndFlush(any())).willReturn(member);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(0L, result.getEarnedCoins());
        assertEquals(0, result.getCorrectCount());
        assertEquals(1L, member.getStreak()); // streak은 +1
    }

    @Test
    @DisplayName("테스트 완료 - 어제 제출 기록이 있으면 오늘 첫 제출로 처리")
    void completeTest_YesterdayRecord_TreatedAsFirstSubmit() {
        // given
        MemberVoca yesterdayRecord = MemberVoca.builder()
                .member(member).voca(voca)
                .correctCnt(1L).learningWordCnt(2L)
                .solvedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1))
                .build();

        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(1L, "apple")));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.of(yesterdayRecord));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1));
        given(memberRepository.saveAndFlush(any())).willReturn(member);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(5L, result.getEarnedCoins()); // 첫 제출 5코인
    }

    @Test
    @DisplayName("테스트 완료 - solvedAt이 null인 기록이 있으면 오늘 첫 제출로 처리")
    void completeTest_NullSolvedAt_TreatedAsFirstSubmit() {
        // given
        MemberVoca nullSolvedAtRecord = MemberVoca.builder()
                .member(member).voca(voca)
                .correctCnt(0L).learningWordCnt(0L)
                .build(); // solvedAt = null

        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(1L, "apple")));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.of(nullSolvedAtRecord));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1));
        given(memberRepository.saveAndFlush(any())).willReturn(member);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(5L, result.getEarnedCoins()); // 첫 제출 5코인
    }

    @Test
    @DisplayName("테스트 완료 - 답안이 빈 리스트면 correctCount=0, earnedCoins=0")
    void completeTest_EmptyAnswers() {
        // given
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of());

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of());
        given(memberRepository.saveAndFlush(any())).willReturn(member);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        VocaResDTO.TestResult result = vocaService.completeTest(1L, authMember, dto);

        // then
        assertNotNull(result);
        assertEquals(0, result.getCorrectCount());
        assertEquals(0, result.getWrongCount());
        assertEquals(0L, result.getEarnedCoins());
        assertTrue(result.getResults().isEmpty());
    }


    @Test
    @DisplayName("테스트 완료 - 오늘 다른 단어장을 이미 풀었으면 스트릭 증가 없음 (핵심 버그 케이스)")
    void completeTest_DifferentVoca_AlreadyStudiedToday_StreakNotIncreases() {
        // given - 오늘 이미 다른 단어장을 풀어서 lastStudiedAt = 오늘인 상태
        member.updateStreak(); // streak = 1, lastStudiedAt = 오늘
        long streakBefore = member.getStreak();

        Voca voca2 = Voca.builder().id(2L).description("다른 단어장").createdAt(LocalDateTime.now()).build();
        Word word3 = Word.builder().id(3L).englishWord("cherry").meaning("체리").voca(voca2).build();
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(3L, "cherry")));

        given(vocaRepository.findById(2L)).willReturn(Optional.of(voca2));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca2)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of(word3));
        given(memberRepository.saveAndFlush(any())).willReturn(member);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        vocaService.completeTest(2L, authMember, dto);

        // then
        assertEquals(streakBefore, member.getStreak()); // 스트릭 유지
    }

    @Test
    @DisplayName("테스트 완료 - lastStudiedAt이 어제인 경우 오늘 첫 제출로 스트릭 증가")
    void completeTest_LastStudiedAtYesterday_StreakIncreases() {
        // given - lastStudiedAt = 어제
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        setField(member, "lastStudiedAt", yesterday);
        setField(member, "streak", 3L);
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(1L, "apple")));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.empty());
        given(wordRepository.findAllById(any())).willReturn(List.of(word1));
        given(memberRepository.saveAndFlush(any())).willReturn(member);
        given(memberVocaRepository.save(any(MemberVoca.class))).willReturn(null);

        // when
        vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(4L, member.getStreak()); // 3 → 4
    }

    @Test
    @DisplayName("테스트 완료 - 같은 단어장 당일 재제출 시 스트릭 유지")
    void completeTest_SameVocaResubmit_StreakNotIncreases() {
        // given - lastStudiedAt = 오늘 (이미 이 단어장을 풀었음)
        member.updateStreak(); // streak = 1, lastStudiedAt = 오늘
        long streakBefore = member.getStreak();

        MemberVoca existing = MemberVoca.builder()
                .member(member).voca(voca)
                .correctCnt(1L).learningWordCnt(2L)
                .solvedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        VocaReqDTO.CompleteTest dto = makeCompleteTestDto(List.of(makeAnswer(1L, "apple")));

        given(vocaRepository.findById(1L)).willReturn(Optional.of(voca));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberVocaRepository.findByMemberAndVoca(member, voca)).willReturn(Optional.of(existing));
        given(wordRepository.findAllById(any())).willReturn(List.of(word1));
        given(memberRepository.saveAndFlush(any())).willReturn(member);

        // when
        vocaService.completeTest(1L, authMember, dto);

        // then
        assertEquals(streakBefore, member.getStreak()); // 스트릭 유지
    }

    private VocaReqDTO.CompleteTest makeCompleteTestDto(List<VocaReqDTO.CompleteTest.Answer> answers) {
        VocaReqDTO.CompleteTest dto = new VocaReqDTO.CompleteTest();
        setField(dto, "answers", answers);
        return dto;
    }

    private VocaReqDTO.CompleteTest.Answer makeAnswer(Long wordId, String answer) {
        VocaReqDTO.CompleteTest.Answer a = new VocaReqDTO.CompleteTest.Answer();
        setField(a, "wordId", wordId);
        setField(a, "answer", answer);
        return a;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
