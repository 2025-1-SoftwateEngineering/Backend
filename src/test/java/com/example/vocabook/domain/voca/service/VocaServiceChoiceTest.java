package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberChoice;
import com.example.vocabook.domain.member.repository.MemberChoiceRepository;
import com.example.vocabook.domain.voca.code.VocaErrorCode;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;
import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.repository.ChoiceQuestionRepository;
import com.example.vocabook.domain.voca.repository.ChoiceRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VocaServiceChoiceTest {

    @InjectMocks
    private VocaService vocaService;

    @Mock
    private ChoiceRepository choiceRepository;

    @Mock
    private ChoiceQuestionRepository choiceQuestionRepository;

    @Mock
    private MemberChoiceRepository memberChoiceRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private RedisUtil redisUtil;

    private AuthMember authMember;
    private Member member;
    private Choice choice;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .coin(0L)
                .choiceHigher(0L)
                .build();
        authMember = new AuthMember(member);

        choice = Choice.builder()
                .id(1L)
                .solvedCoin(100L)
                .build();
    }

    @Test
    @DisplayName("사지선다 목록 조회 성공")
    void getChoiceListSuccess() {
        // given
        when(choiceRepository.findChoiceWithoutCursor(any(PageRequest.class)))
                .thenReturn(new SliceImpl<>(List.of(choice)));
        when(memberChoiceRepository.countByMemberAndChoice(eq(member), eq(choice)))
                .thenReturn(3L);

        // when
        PagingResDTO.Cursor<VocaResDTO.GetChoiceList> result = vocaService.getChoiceList(authMember, "-1", 10);

        // then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).id()).isEqualTo(1L);
        assertThat(result.data().get(0).solvedCoin()).isEqualTo(100L);
        assertThat(result.data().get(0).cnt()).isEqualTo(3L);
    }

    @Test
    @DisplayName("사지선다 선택지 조회 성공 - 첫 문제 (current=0)")
    void getChoiceSuccess_FirstQuestion() {
        // given
        Word mockWord = Word.builder().id(10L).englishWord("apple").meaning("사과").build();
        ChoiceQuestion firstQuestion = ChoiceQuestion.builder()
                .id(10L).choice(choice).word(mockWord).isWord(true).build();
        
        when(choiceRepository.findById(choice.getId())).thenReturn(Optional.of(choice));
        when(choiceQuestionRepository.findByChoiceWithCurrent(choice.getId(), 0L))
                .thenReturn(Optional.of(firstQuestion));
        when(memberChoiceRepository.findByMemberAndChoiceAndSolvedAtIsNull(member, choice))
                .thenReturn(Optional.empty()); // No active session
        
        MemberChoice activeSession = MemberChoice.builder()
                .id(100L).member(member).choice(choice).score(0L).build();
        when(memberChoiceRepository.save(any(MemberChoice.class)))
                .thenReturn(activeSession);
        
        // 3 dummy words
        Word dummy1 = Word.builder().id(11L).englishWord("b1").meaning("b1m").build();
        Word dummy2 = Word.builder().id(12L).englishWord("b2").meaning("b2m").build();
        Word dummy3 = Word.builder().id(13L).englishWord("b3").meaning("b3m").build();
        
        when(wordRepository.findRandomExcluding(mockWord.getId(), 3))
                .thenReturn(List.of(dummy1, dummy2, dummy3));

        // when
        VocaResDTO.GetChoice result = vocaService.getChoice(choice.getId(), 0L, authMember);

        // then
        assertThat(result.id()).isEqualTo(100L); // memberChoiceId
        assertThat(result.question()).isEqualTo("apple");
        assertThat(result.choices()).hasSize(4); // 1 correct + 3 random
        verify(redisUtil).save(eq("choice:1:1:10"), any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("사지선다 선택지 조회 성공 - 더 이상 문제가 없는 경우 (No Content)")
    void getChoiceSuccess_NoMoreQuestions() {
        // given
        when(choiceRepository.findById(choice.getId())).thenReturn(Optional.of(choice));
        when(choiceQuestionRepository.findByChoiceWithCurrent(choice.getId(), 10L))
                .thenReturn(Optional.empty()); // No more questions

        // when
        VocaResDTO.GetChoice result = vocaService.getChoice(choice.getId(), 10L, authMember);

        // then
        assertThat(result).isNull(); // Service returns null -> Controller gives 204
    }

    @Test
    @DisplayName("사지선다 정답 제출 성공 - 정답 (Timer 정상)")
    void submitChoiceSuccess_Correct() {
        // given
        Word mockWord = Word.builder().id(100L).englishWord("apple").meaning("사과").build();
        ChoiceQuestion currentQ = ChoiceQuestion.builder()
                .id(10L).choice(choice).word(mockWord).isWord(true).build();
        MemberChoice activeSession = MemberChoice.builder()
                .member(member).choice(choice).score(0L).build();
        
        when(choiceRepository.findById(choice.getId())).thenReturn(Optional.of(choice));
        when(memberChoiceRepository.findByMemberAndChoiceAndSolvedAtIsNull(member, choice))
                .thenReturn(Optional.of(activeSession));
        when(choiceQuestionRepository.findByChoiceWithCurrent(choice.getId(), 10L))
                .thenReturn(Optional.of(currentQ));
        
        // Redis recorded time = 1 second ago (valid)
        when(redisUtil.get("choice:1:1:10")).thenReturn(LocalDateTime.now().minusSeconds(1));
        
        when(wordRepository.findById(100L)).thenReturn(Optional.of(mockWord));

        ChoiceQuestion nextQ = ChoiceQuestion.builder().id(11L).build();
        when(choiceQuestionRepository.findNextQuestion(10L)).thenReturn(Optional.of(nextQ));

        // when (answer=100 matches currentQ.word.id)
        VocaResDTO.SubmitChoice result = vocaService.submitChoice(authMember, choice.getId(), 100L, 10L);

        // then
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.score()).isGreaterThan(0L); // 1초 경과로 점수 획득
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCurrent()).isEqualTo(10L); // VocaConverter.toSubmitChoice receives choiceQuestion.getId()
        assertThat(activeSession.getScore()).isEqualTo(result.score());
    }

    @Test
    @DisplayName("사지선다 정답 제출 실패 - 진행 중인 게임이 없음")
    void submitChoiceException_NotPlaying() {
        // given
        when(choiceRepository.findById(choice.getId())).thenReturn(Optional.of(choice));
        when(memberChoiceRepository.findByMemberAndChoiceAndSolvedAtIsNull(member, choice))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vocaService.submitChoice(authMember, choice.getId(), 100L, 10L))
                .isInstanceOf(VocaException.class)
                .extracting("code").isEqualTo(VocaErrorCode.NOT_PLAY_CHOICE);
    }
}
