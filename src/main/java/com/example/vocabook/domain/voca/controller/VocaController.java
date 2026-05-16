package com.example.vocabook.domain.voca.controller;

import com.example.vocabook.domain.voca.controller.docs.VocaControllerDocs;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.code.VocaSuccessCode;
import com.example.vocabook.domain.voca.service.VocaService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VocaController implements VocaControllerDocs {

	private final VocaService vocaService;

	@GetMapping
	public ApiResponse<VocaResDTO.VocaList> getVocaList(
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_VOCA_LIST, vocaService.getVocaList(authMember));
	}

	@PostMapping("/{vocaId}/memorize")
	public ApiResponse<VocaResDTO.MemorizeInfo> memorizeWords(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.Memorize dto
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.MEMORIZE, vocaService.memorizeWords(vocaId, authMember, dto));
	}

	@GetMapping("/{vocaId}/memorize")
	public ApiResponse<VocaResDTO.MemorizeInfo> getMemorizedWords(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_MEMORIZED_WORDS, vocaService.getMemorizedWords(vocaId, authMember));
	}

	@GetMapping("/my")
	public ApiResponse<VocaResDTO.StudiedVocaList> getStudiedVocas(
			@AuthenticationPrincipal AuthMember authMember
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_STUDIED_VOCAS, vocaService.getStudiedVocas(authMember));
	}

	@GetMapping("/v1/voca/{vocaId}/words")
	public ApiResponse<VocaResDTO.WordList> getWords(
			@PathVariable Long vocaId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int pageSize
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_WORDS, vocaService.getWords(vocaId, page, pageSize));
	}

	@GetMapping("/v1/voca/{vocaId}/test")
	public ApiResponse<List<VocaResDTO.TestQuestion>> getTestQuestions(
			@PathVariable Long vocaId
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.GET_TEST, vocaService.getTestQuestions(vocaId));
	}

	@PostMapping("/v1/voca/{vocaId}/test/submit")
	public ApiResponse<VocaResDTO.AnswerResult> submitAnswer(
			@PathVariable Long vocaId,
			@RequestBody @Valid VocaReqDTO.SubmitAnswer dto
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.SUBMIT_TEST, vocaService.submitAnswer(vocaId, dto));
	}

	@PostMapping("/{vocaId}/test/complete")
	public ApiResponse<VocaResDTO.TestResult> completeTest(
			@PathVariable Long vocaId,
			@AuthenticationPrincipal AuthMember authMember,
			@RequestBody @Valid VocaReqDTO.CompleteTest dto
	) {
		return ApiResponse.onSuccess(VocaSuccessCode.COMPLETE_TEST, vocaService.completeTest(vocaId, authMember, dto));
	}

    // 사지선다 문제 목록 조회
    @GetMapping("/v1/choices")
    public ApiResponse<PagingResDTO.Cursor<VocaResDTO.GetChoiceList>> getChoiceList(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        BaseSuccessCode code = VocaSuccessCode.GET_CHOICE_LIST;
        return ApiResponse.onSuccess(code, vocaService.getChoiceList(auth, cursor, pageSize));
    }

    // 사지선다 문제 중 선택지 조회
    @GetMapping("/v1/choices/{choiceId}")
    public ResponseEntity<ApiResponse<VocaResDTO.GetChoice>> getChoice(
            @PathVariable Long choiceId,
            @RequestParam Long current,
            @AuthenticationPrincipal AuthMember auth
    ){
        VocaResDTO.GetChoice result = vocaService.getChoice(choiceId, current, auth);

        if (result != null) {
            BaseSuccessCode code = VocaSuccessCode.GET_CHOICE;
            return ResponseEntity.ok().body(ApiResponse.onSuccess(code, result));
        } else {
            BaseSuccessCode code = VocaSuccessCode.GET_CHOICE_NO_CONTENT;
            return ResponseEntity.status(code.getStatus())
                    .body(ApiResponse.onSuccess(code, null));
        }
    }

    // 사지선다 문제 답안 제출
    @PostMapping("/v1/choices/{choiceId}/submit")
    public ApiResponse<VocaResDTO.SubmitChoice> submitChoice(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long choiceId,
            @RequestParam Long answer,
            @RequestParam Long current
    ){
        BaseSuccessCode code = VocaSuccessCode.SUBMIT_CHOICE;
        return ApiResponse.onSuccess(code, vocaService.submitChoice(auth, choiceId, answer, current));
    }

    // 십자말풀이 목록 조회
    @GetMapping("/v1/crosswords")
    public ApiResponse<PagingResDTO.Cursor<VocaResDTO.GetCrosswordList>> getCrosswordList(
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        BaseSuccessCode code = VocaSuccessCode.GET_CROSSWORD_LIST;
        return ApiResponse.onSuccess(code, vocaService.getCrosswordList(auth, cursor, pageSize));
    }

    // 십자말풀이 문제 조회
    @GetMapping("/v1/crosswords/{crosswordId}")
    public ApiResponse<VocaResDTO.GetCrossword> getCrossword(
            @PathVariable Long crosswordId,
            @AuthenticationPrincipal AuthMember auth
    ){
        BaseSuccessCode code = VocaSuccessCode.GET_CROSSWORD;
        return ApiResponse.onSuccess(code, vocaService.getCrossword(crosswordId, auth));
    }

    // 십자말풀이 한 문제 정답 제출
    @PostMapping("/v1/crosswords/{crosswordId}/submit")
    public ApiResponse<VocaResDTO.SubmitCrossword> submitCrossword(
            @PathVariable Long crosswordId,
            @AuthenticationPrincipal AuthMember auth,
            @RequestParam Long crosswordHintId,
            @RequestParam String answer
    ){
        BaseSuccessCode code = VocaSuccessCode.SUBMIT_CROSSWORD;
        return ApiResponse.onSuccess(code, vocaService.submitCrossword(crosswordId, auth, crosswordHintId, answer));
    }
}
