package com.example.vocabook.domain.voca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

public class VocaReqDTO {

	@Getter
	public static class Memorize {

		@NotNull
		private List<Long> wordIds;
	}

	// 단어 하나씩 즉시 채점
	@Getter
	public static class SubmitAnswer {

		@NotNull
		private Long wordId;

		@NotNull
		private String answer;
	}

	// 테스트 전체 완료 (코인/스트릭 처리용)
	@Getter
	public static class CompleteTest {

		@NotNull
		@Valid
		private List<Answer> answers;

		@Getter
		public static class Answer {

			@NotNull
			private Long wordId;

			@NotNull
			private String answer;
		}
	}
}
