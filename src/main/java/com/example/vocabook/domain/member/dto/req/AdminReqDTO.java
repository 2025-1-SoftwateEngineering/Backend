package com.example.vocabook.domain.member.dto.req;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class AdminReqDTO {

    // 단어장 추가
    public record AddVocabulary(
            @NotBlank(message = "설명은 빈칸일 수 없습니다.")
            String description,
            @NotNull(message = "해결 완료 시 지급하는 코인의 양은 필수입니다.")
            Long solvedCoin
    ) {}

    // 단어 추가
    public record AddWord(
            @NotBlank(message = "영어는 빈칸일 수 없습니다.")
            String english,
            @NotBlank(message = "설명은 빈칸일 수 없습니다.")
            String meaning,
            @Nullable
            Long vocabularyId
    ) {}
    // 단어장 수정
    public record UpdateVocabulary(
            @Nullable
            @Size(min = 1, message = "설명은 빈칸일 수 없습니다.")
            String description,
            @Nullable
            @PositiveOrZero(message = "코인은 0 이상이어야 합니다.")
            Long solvedCoin
    ) {}

    // 단어 수정
    public record UpdateWord(
            @Nullable
            @Size(min = 1, message = "영어는 빈칸일 수 없습니다.")
            String english,
            @Nullable
            @Size(min = 1, message = "설명은 빈칸일 수 없습니다.")
            String meaning,
            @Nullable
            Long vocabularyId
    ) {}
}
