package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.AdminErrorCode;
import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.exception.AdminException;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.repository.VocaRepository;
import com.example.vocabook.domain.voca.repository.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vocabook.domain.voca.enums.ClueType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceExceptionTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private VocaRepository vocaRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private com.example.vocabook.domain.voca.repository.CrosswordRepository crosswordRepository;

    @Mock
    private com.example.vocabook.domain.voca.repository.CrosswordHintRepository crosswordHintRepository;

    @Test
    @DisplayName("단어장 수정 실패 - 존재하지 않는 단어장")
    void updateVocabularyException_NotFound() {
        // given
        Long vocaId = 999L;
        AdminReqDTO.UpdateVocabulary dto = new AdminReqDTO.UpdateVocabulary("Test", 100L);
        when(vocaRepository.findById(vocaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.updateVocabulary(vocaId, dto))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.VOCA_NOT_FOUND);
    }

    @Test
    @DisplayName("단어장 삭제 실패 - 존재하지 않는 단어장")
    void deleteVocabularyException_NotFound() {
        // given
        Long vocaId = 999L;
        when(vocaRepository.findById(vocaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.deleteVocabulary(vocaId))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.VOCA_NOT_FOUND);
    }

    @Test
    @DisplayName("단어 수정 실패 - 존재하지 않는 단어")
    void updateWordException_WordNotFound() {
        // given
        Long wordId = 999L;
        AdminReqDTO.UpdateWord dto = new AdminReqDTO.UpdateWord("apple", "사과", 2L);
        when(wordRepository.findById(wordId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.updateWord(wordId, dto))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.WORD_NOT_FOUND);
    }

    @Test
    @DisplayName("단어 수정 실패 - 존재하지 않는 단어장 매핑")
    void updateWordException_VocaNotFound() {
        // given
        Long wordId = 1L;
        AdminReqDTO.UpdateWord dto = new AdminReqDTO.UpdateWord("apple", "사과", 999L);
        Word mockWord = Word.builder().englishWord("oldApple").meaning("오래된사과").build();

        when(wordRepository.findById(wordId)).thenReturn(Optional.of(mockWord));
        when(vocaRepository.findById(dto.vocabularyId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.updateWord(wordId, dto))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.VOCA_NOT_FOUND);
    }

    @Test
    @DisplayName("단어 삭제 실패 - 존재하지 않는 단어")
    void deleteWordException_NotFound() {
        // given
        Long wordId = 999L;
        when(wordRepository.findById(wordId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.deleteWord(wordId))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.WORD_NOT_FOUND);
    }

    @Test
    @DisplayName("사지선다 문제 생성 실패 - 포함할 단어/뜻이 DB에 하나도 없음")
    void createChoiceException_WordsNotFound() {
        // given
        AdminReqDTO.ChoiceList choice1 = new AdminReqDTO.ChoiceList("unknownApple", true);
        AdminReqDTO.CreateChoice dto = new AdminReqDTO.CreateChoice(100L, java.util.List.of(choice1));

        when(wordRepository.findAllByEnglishWordInOrMeaningIn(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(java.util.Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> adminService.createChoice(dto))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.WORD_NOT_FOUND);
    }

    @Test
    @DisplayName("십자말풀이 문제 생성 실패 - 시작점 좌표 정규식 위반")
    void createCrosswordsException_InvadeStartRegex() {
        // given
        AdminReqDTO.Crossword cwDto = new AdminReqDTO.Crossword(ClueType.ACROSS, "달콤한 과일", "1-1", "apple"); // "1 1"이어야 하는데 "1-1"로 잘못 입력
        AdminReqDTO.CreateCrossword dto = new AdminReqDTO.CreateCrossword(200L, java.util.List.of(cwDto));
        
        Word mockWord = Word.builder().englishWord("apple").meaning("사과").build();
        com.example.vocabook.domain.voca.entity.Crossword savedCrossword = com.example.vocabook.domain.voca.entity.Crossword.builder().id(10L).solvedCoin(200L).build();

        // AdminService.java의 첫 번째 줄에서 crosswordRepository.save()가 호출되므로 Mocking 필요
        when(crosswordRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(savedCrossword);
        
        // 십자말풀이 저장까진 넘어가고 힌트 생성 단계에서 예외 발생
        when(wordRepository.findFirstByEnglishWord("apple")).thenReturn(Optional.of(mockWord));

        // when & then
        assertThatThrownBy(() -> adminService.createCrosswords(dto))
                .isInstanceOf(AdminException.class)
                .extracting("code").isEqualTo(AdminErrorCode.INVADE_START_REGEX);
    }
}
