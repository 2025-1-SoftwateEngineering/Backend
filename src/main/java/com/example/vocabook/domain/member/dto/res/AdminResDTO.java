package com.example.vocabook.domain.member.dto.res;

import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.member.enums.ReportReason;
import com.example.vocabook.domain.store.enums.ItemType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class AdminResDTO {

    // 신고 목록 조회
    @Builder
    public record ReportList(
            Long targetMemberId,
            ReportReason reason,
            String detailReason,
            Long reportCnt
    ) {}

    // 영구 정지
    @Builder
    public record Suspend(
            Long targetMemberId
    ) {}

    // 단어장 추가
    @Builder
    public record AddVocabulary(
            Long id,
            LocalDateTime addedAt
    ) {}

    // 단어장 목록 조회
    @Builder
    public record GetVocabularyList(
            Long id,
            String description,
            LocalDateTime addedAt,
            List<WordList> wordList
    ) {}

    @Builder
    public record WordList(
            String english,
            String meaning
    ) {}

    // 단어 추가
    @Builder
    public record AddWord(
            Long id,
            String english,
            String meaning,
            Long vocabularyId
    ) {}

    // 단어 검색
    @Builder
    public record SearchWord(
            Long id,
            String english,
            String meaning,
            Long vocabularyId
    ) {}
    // 단어장 수정
    @Builder
    public record UpdateVocabulary(
            Long id,
            String description,
            Long solvedCoin
    ) {}

    // 단어장 삭제
    @Builder
    public record DeleteVocabulary(
            Long id
    ) {}

    // 단어 수정
    @Builder
    public record UpdateWord(
            Long id,
            String english,
            String meaning,
            Long vocabularyId
    ) {}

    // 단어 삭제
    @Builder
    public record DeleteWord(
            Long id
    ) {}

    // 사지선다 문제 생성
    @Builder
    public record CreateChoice(
            String word,
            Boolean isWord
    ) {}

    // 십자말풀이 문제 생성
    @Builder
    public record CreateCrossword(
            Long id,
            Long solvedCoin
    ) {}

    // 사진 업로드 완료
    @Builder
    public record UploadImage(
            String publicUrl,
            LocalDateTime uploadAt
    ) {}

    // 사진 업로드용 URI 생성
    @Builder
    public record CreateSignedUri(
            String url,
            String fileName,
            PhotoType photoType
    ) {}

    // 아이템 생성
    @Builder
    public record CreateItem(
            Long id,
            String name,
            ItemType itemType,
            Long price
    ) {}

}
