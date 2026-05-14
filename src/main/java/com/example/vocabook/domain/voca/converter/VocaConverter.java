package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public class VocaConverter {

    public static Voca toVoca(
            AdminReqDTO.AddVocabulary dto
    ){
        return Voca.builder()
                .createdAt(LocalDateTime.now())
                .description(dto.description())
                .solvedCoin(dto.solvedCoin())
                .build();
    }

	public static VocaResDTO.WordList toWordList(Page<Word> wordPage) {
		List<VocaResDTO.WordInfo> words = wordPage.getContent().stream()
				.map(word -> VocaResDTO.WordInfo.builder()
						.wordId(word.getId())
						.englishWord(word.getEnglishWord())
						.meaning(word.getMeaning())
						.build())
				.toList();
		return VocaResDTO.WordList.builder()
				.words(words)
				.totalPages(wordPage.getTotalPages())
				.totalElements(wordPage.getTotalElements())
				.build();
	}

	public static VocaResDTO.TestQuestion toTestQuestion(Word word) {
		return VocaResDTO.TestQuestion.builder()
				.wordId(word.getId())
				.englishWord(word.getEnglishWord())
				.meaning(word.getMeaning())
				.build();
	}

	// 학습한 단어장 목록 변환
	public static VocaResDTO.StudiedVocaList toStudiedVocaList(List<MemberVoca> memberVocas) {
		List<VocaResDTO.StudiedVocaInfo> vocas = memberVocas.stream()
				.map(mv -> VocaResDTO.StudiedVocaInfo.builder()
						.vocaId(mv.getVoca().getId())
						.description(mv.getVoca().getDescription())
						.learningWordCnt(mv.getLearningWordCnt())
						.correctCnt(mv.getCorrectCnt())
						.solvedAt(mv.getSolvedAt())
						.build())
				.toList();
		return VocaResDTO.StudiedVocaList.builder()
				.vocas(vocas)
				.totalCount(vocas.size())
				.build();
	}
}