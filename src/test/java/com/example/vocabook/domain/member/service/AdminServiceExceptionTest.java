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
}
