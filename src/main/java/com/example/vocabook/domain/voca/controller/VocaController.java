package com.example.vocabook.domain.voca.controller;

import com.example.vocabook.domain.voca.controller.docs.VocaControllerDocs;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.exception.code.VocaSuccessCode;
import com.example.vocabook.domain.voca.service.VocaService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/voca")
public class VocaController implements VocaControllerDocs {

	private final VocaService vocaService;

	@GetMapping("/{vocaId}/words")
	public ApiResponse<VocaResDTO.WordList> getWords(
			@PathVariable Long vocaId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int pageSize
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_WORDS, vocaService.getWords(vocaId, page, pageSize));
	}

	@GetMapping("/{vocaId}/test")
	public ApiResponse<List<VocaResDTO.TestQuestion>> getTestQuestions(
			@PathVariable Long vocaId
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_TEST, vocaService.getTestQuestions(vocaId));
	}

	@PostMapping("/{vocaId}/test/submit")
	public ApiResponse<VocaResDTO.TestResult> submitTest(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.SubmitTest dto
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.SUBMIT_TEST, vocaService.submitTest(vocaId, authMember, dto));
	}
}