package com.example.vocabook.domain.voca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

public class VocaReqDTO {

	@Getter
	public static class SubmitTest {

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