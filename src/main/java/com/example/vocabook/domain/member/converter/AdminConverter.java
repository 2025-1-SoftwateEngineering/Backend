package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.res.AdminResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;

import java.util.List;

public class AdminConverter {

    // 신고 목록 조회
    public static AdminResDTO.ReportList toReportList(
            Report report,
            Long cnt
    ) {
        return AdminResDTO.ReportList.builder()
                .targetMemberId(report.getTargetMember().getId())
                .detailReason(report.getDetailReason())
                .reason(report.getReportReason())
                .reportCnt(cnt)
                .build();
    }

    // 영구 정지
    public static AdminResDTO.Suspend toSuspend(
            Member member
    ) {
        return AdminResDTO.Suspend.builder()
                .targetMemberId(member.getId())
                .build();
    }

    // 단어장 생성
    public static AdminResDTO.AddVocabulary toAddVocabulary(
            Voca voca
    ){
        return AdminResDTO.AddVocabulary.builder()
                .id(voca.getId())
                .addedAt(voca.getCreatedAt())
                .build();
    }

    // 단어장 목록 조회
    public static AdminResDTO.GetVocabularyList toGetVocabularyList(
            Voca voca,
            List<Word> words
    ) {
        return AdminResDTO.GetVocabularyList.builder()
                .id(voca.getId())
                .description(voca.getDescription())
                .addedAt(voca.getCreatedAt())
                .wordList(words.stream().map(AdminConverter::toWordList).toList())
                .build();
    }

    public static AdminResDTO.WordList toWordList(
            Word word
    ) {
        return AdminResDTO.WordList.builder()
                .english(word.getEnglishWord())
                .meaning(word.getMeaning())
                .build();
    }

    // 단어 생성
    public static AdminResDTO.AddWord toAddWord(
            Word word
    ){
        return AdminResDTO.AddWord.builder()
                .id(word.getId())
                .english(word.getEnglishWord())
                .meaning(word.getMeaning())
                .vocabularyId((word.getVoca() != null) ? word.getVoca().getId(): null)
                .build();
    }

    // 단어 검색
    public static AdminResDTO.SearchWord toSearchWord(
            Word word
    ) {
        if (word == null) return null;
        return AdminResDTO.SearchWord.builder()
                .id(word.getId())
                .english(word.getEnglishWord())
                .meaning(word.getMeaning())
                .vocabularyId((word.getVoca() != null) ? word.getVoca().getId() : null)
                .build();
    }
    // 단어장 수정
    public static AdminResDTO.UpdateVocabulary toUpdateVocabulary(
            Voca voca
    ) {
        return AdminResDTO.UpdateVocabulary.builder()
                .id(voca.getId())
                .description(voca.getDescription())
                .solvedCoin(voca.getSolvedCoin())
                .build();
    }

    // 단어장 삭제
    public static AdminResDTO.DeleteVocabulary toDeleteVocabulary(
            Long vocaId
    ) {
        return AdminResDTO.DeleteVocabulary.builder()
                .id(vocaId)
                .build();
    }

    // 단어 수정
    public static AdminResDTO.UpdateWord toUpdateWord(
            Word word
    ) {
        return AdminResDTO.UpdateWord.builder()
                .id(word.getId())
                .english(word.getEnglishWord())
                .meaning(word.getMeaning())
                .vocabularyId((word.getVoca() != null) ? word.getVoca().getId() : null)
                .build();
    }

    // 단어 삭제
    public static AdminResDTO.DeleteWord toDeleteWord(
            Long wordId
    ) {
        return AdminResDTO.DeleteWord.builder()
                .id(wordId)
                .build();
    }

    // 사지선다 생성
    public static AdminResDTO.CreateChoice toCreateChoice(
            ChoiceQuestion result
    ) {
        return AdminResDTO.CreateChoice.builder()
                .isWord(result.getIsWord())
                .word((result.getIsWord()) ? result.getWord().getEnglishWord():result.getWord().getMeaning())
                .build();
    }
}
