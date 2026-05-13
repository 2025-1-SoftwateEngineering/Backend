package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.dto.res.AdminResDTO;
import com.example.vocabook.domain.voca.entity.Voca;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceSuccessTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private VocaRepository vocaRepository;

    @Mock
    private WordRepository wordRepository;

    @Test
    @DisplayName("단어장 수정 성공")
    void updateVocabularySuccess() {
        // given
        Long vocaId = 1L;
        AdminReqDTO.UpdateVocabulary dto = new AdminReqDTO.UpdateVocabulary("Updated Voca", 100L);
        Voca mockVoca = Voca.builder().description("Old Voca").solvedCoin(50L).build();
        
        when(vocaRepository.findById(vocaId)).thenReturn(Optional.of(mockVoca));

        // when
        AdminResDTO.UpdateVocabulary result = adminService.updateVocabulary(vocaId, dto);

        // then
        assertThat(result.description()).isEqualTo("Updated Voca");
        assertThat(result.solvedCoin()).isEqualTo(100L);
    }

    @Test
    @DisplayName("단어장 삭제 성공")
    void deleteVocabularySuccess() {
        // given
        Long vocaId = 1L;
        Voca mockVoca = Voca.builder().description("Voca to delete").build();
        
        when(vocaRepository.findById(vocaId)).thenReturn(Optional.of(mockVoca));

        // when
        AdminResDTO.DeleteVocabulary result = adminService.deleteVocabulary(vocaId);

        // then
        verify(vocaRepository, times(1)).delete(mockVoca);
        assertThat(result.id()).isEqualTo(vocaId);
    }

    @Test
    @DisplayName("단어 수정 성공")
    void updateWordSuccess() {
        // given
        Long wordId = 1L;
        AdminReqDTO.UpdateWord dto = new AdminReqDTO.UpdateWord("apple", "사과", 2L);
        Word mockWord = Word.builder().englishWord("oldApple").meaning("오래된사과").build();
        Voca mockVoca = Voca.builder().description("Fruit Voca").build();

        when(wordRepository.findById(wordId)).thenReturn(Optional.of(mockWord));
        when(vocaRepository.findById(2L)).thenReturn(Optional.of(mockVoca));

        // when
        AdminResDTO.UpdateWord result = adminService.updateWord(wordId, dto);

        // then
        assertThat(result.english()).isEqualTo("apple");
        assertThat(result.meaning()).isEqualTo("사과");
    }

    @Test
    @DisplayName("단어 삭제 성공")
    void deleteWordSuccess() {
        // given
        Long wordId = 1L;
        Word mockWord = Word.builder().englishWord("apple").meaning("사과").build();

        when(wordRepository.findById(wordId)).thenReturn(Optional.of(mockWord));

        // when
        AdminResDTO.DeleteWord result = adminService.deleteWord(wordId);

        // then
        verify(wordRepository, times(1)).delete(mockWord);
        assertThat(result.id()).isEqualTo(wordId);
    }
}
