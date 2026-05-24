package com.example.vocabook.domain.voca.dto;

import com.example.vocabook.domain.voca.enums.ClueType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class VocaResDTO {

	@Getter
	@Builder
	@AllArgsConstructor
	public static class WordList {
		private Long vocaId;
		private String description;
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
	public static class VocaInfo {
		private Long vocaId;
		private String description;
		private long wordCount;
		private long memorizedCount;
		private LocalDateTime createdAt;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class VocaList {
		private List<VocaInfo> vocas;
		private int totalCount;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class MemorizeInfo {
		private List<Long> memorizedWordIds;
		private int totalCount;
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

    // 사지선다 문제 목록 조회
    @Builder
    public record GetChoiceList(
            Long id,
            Long solvedCoin,
            Long cnt
    ) {}

    // 사지선다 선택지 목록 조회 (단어 뜻 구분 X)
    @Builder
    public record GetChoice(
            Long id,
            Long score,
            String question,
            List<ChoiceElement> choices,
            int totalTime
    ) {}

    @Builder
    public record ChoiceElement(
            Long id,
            String text
    ) {}

    // 사지선다 정답 제출
    @Builder
    public record SubmitChoice(
            Boolean isCorrect,
            Boolean hasNext,
            Long nextCurrent,
            Long score
    ) {}

    // 십자말풀이 문제 목록 조회
    @Builder
    public record GetCrosswordList(
            Long id,
            Long solvedCoin,
            Long cnt
    ) {}

    // 십자말풀이
    @Builder
    public record GetCrossword(
            Integer N,
            List<CrosswordElement> elements
    ) {}

    @Builder
    public record CrosswordElement(
            Long id,
            ClueType clueType,
            Integer wordLength,
            Integer verticalStartPoint,
            Integer horizontalStartPoint,
            String clueDescription
    ) {}

    // 십자말풀이 정답 제출
    @Builder
    public record SubmitCrossword(
            Boolean isCorrect,
            Boolean hasNext,
            Duration score
    ) {}
}
