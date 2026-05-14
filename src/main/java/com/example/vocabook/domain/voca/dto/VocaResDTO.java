package com.example.vocabook.domain.voca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
		private int wrongCount;
		private long earnedCoins;
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

	@Getter
	@Builder
	@AllArgsConstructor
	public static class StudiedVocaList {
		private List<StudiedVocaInfo> vocas;
		private int totalCount;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class StudiedVocaInfo {
		private Long vocaId;
		private String description;
		private Long learningWordCnt;
		private Long correctCnt;
		private LocalDateTime solvedAt;
	}
}