package com.example.vocabook.domain.voca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VocaResDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	public static class WordList {
		private List<WordInfo> words;
		private int totalPages;
		private long totalElements;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class WordInfo {
		private Long wordId;
		private String englishWord;
		private String meaning;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class TestQuestion {
		private Long wordId;
		private String englishWord;
		private String meaning;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class TestResult {
		private int totalCount;
		private int correctCount;
		private List<AnswerResult> results;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class AnswerResult {
		private Long wordId;
		private String meaning;
		private String correctAnswer;
		private String submittedAnswer;
		@JsonProperty("isCorrect")
		private boolean correct;
	}
}