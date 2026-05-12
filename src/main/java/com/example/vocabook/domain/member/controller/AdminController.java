package com.example.vocabook.domain.member.controller;

import com.example.vocabook.domain.member.code.AdminErrorCode;
import com.example.vocabook.domain.member.code.AdminSuccessCode;
import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.member.dto.res.AdminResDTO;
import com.example.vocabook.domain.member.exception.AdminException;
import com.example.vocabook.domain.member.service.AdminService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.vocabook.domain.member.controller.docs.AdminControllerDocs;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController implements AdminControllerDocs {

    private final AdminService adminService;

    // 신고 목록 조회
    @GetMapping("/v1/reports")
    public ApiResponse<PagingResDTO.Cursor<AdminResDTO.ReportList>> getReportList(
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        BaseSuccessCode code = AdminSuccessCode.REPORT_LIST;
        return ApiResponse.onSuccess(code, adminService.getReportList(cursor, pageSize));
    }

    // 사용자 영구 차단
    @PostMapping("/v1/members/{memberId}/suspend")
    public ApiResponse<AdminResDTO.Suspend> suspend(
            @PathVariable Long memberId
    ){
        BaseSuccessCode code = AdminSuccessCode.SUSPEND;
        return ApiResponse.onSuccess(code, adminService.suspend(memberId));
    }

    // 단어장 추가
    @PostMapping("/v1/voca-books")
    public ApiResponse<List<AdminResDTO.AddVocabulary>> addVocabulary(
            @RequestBody @Valid List<AdminReqDTO.AddVocabulary> dto
    ) {
        BaseSuccessCode code = AdminSuccessCode.ADD_VOCABULARY;

        List<AdminResDTO.AddVocabulary> result = adminService.addVocabulary(dto);

        // 저장할 단어장이 없는 경우
        if (result.isEmpty()) {
            return ApiResponse.onSuccess(AdminSuccessCode.NOT_ADD_VOCABULARY, null);
        } else {
            return ApiResponse.onSuccess(code, result);
        }
    }

    // 단어장 목록 조회
    @GetMapping("/v1/voca-books")
    public ApiResponse<PagingResDTO.Cursor<AdminResDTO.GetVocabularyList>> getVocabularyList(
            @RequestParam(defaultValue = "-1") String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        BaseSuccessCode code = AdminSuccessCode.GET_VOCABULARY_LIST;
        return ApiResponse.onSuccess(code, adminService.getVocabularyList(cursor, pageSize));
    }

    // 단어장 수정
    @PatchMapping("/v1/voca-books/{vocaId}")
    public ApiResponse<AdminResDTO.UpdateVocabulary> updateVocabulary(
            @PathVariable Long vocaId,
            @RequestBody @Valid AdminReqDTO.UpdateVocabulary dto
    ) {
        BaseSuccessCode code = AdminSuccessCode.UPDATE_VOCABULARY;
        return ApiResponse.onSuccess(code, adminService.updateVocabulary(vocaId, dto));
    }

    // 단어장 삭제
    @DeleteMapping("/v1/voca-books/{vocaId}")
    public ApiResponse<AdminResDTO.DeleteVocabulary> deleteVocabulary(
            @PathVariable Long vocaId
    ) {
        BaseSuccessCode code = AdminSuccessCode.DELETE_VOCABULARY;
        return ApiResponse.onSuccess(code, adminService.deleteVocabulary(vocaId));
    }
    // 단어 추가 (단어장이랑 독립적)
    @PostMapping("/v1/words")
    public ApiResponse<List<AdminResDTO.AddWord>> addWord(
            @RequestBody @Valid List<AdminReqDTO.AddWord> dto
    ) {
        BaseSuccessCode code = AdminSuccessCode.ADD_WORD;
        return ApiResponse.onSuccess(code, adminService.addWord(dto));
    }

    // 단어 검색
    @GetMapping("/v1/words/search")
    public ApiResponse<AdminResDTO.SearchWord> searchWord(
            @RequestParam String word
    ) {
        BaseSuccessCode code = AdminSuccessCode.SEARCH_WORD;
        return ApiResponse.onSuccess(code, adminService.searchWord(word));
    }

    // 단어 수정
    @PatchMapping("/v1/words/{wordId}")
    public ApiResponse<AdminResDTO.UpdateWord> updateWord(
            @PathVariable Long wordId,
            @RequestBody @Valid AdminReqDTO.UpdateWord dto
    ) {
        BaseSuccessCode code = AdminSuccessCode.UPDATE_WORD;
        return ApiResponse.onSuccess(code, adminService.updateWord(wordId, dto));
    }

    // 단어 삭제
    @DeleteMapping("/v1/words/{wordId}")
    public ApiResponse<AdminResDTO.DeleteWord> deleteWord(
            @PathVariable Long wordId
    ) {
        BaseSuccessCode code = AdminSuccessCode.DELETE_WORD;
        return ApiResponse.onSuccess(code, adminService.deleteWord(wordId));
    }
}
