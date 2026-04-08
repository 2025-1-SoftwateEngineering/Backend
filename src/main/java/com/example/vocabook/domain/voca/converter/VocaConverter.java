package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.domain.Page;

import java.util.List;

public class VocaConverter {

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
				.meaning(word.getMeaning())
				.build();
	}
}