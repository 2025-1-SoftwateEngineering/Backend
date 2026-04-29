package com.example.vocabook.global.apiPayload.dto;

import lombok.Builder;

import java.util.List;

public class PagingResDTO {

    // 오프셋
    @Builder
    public record Offset<T>(
            List<T> data,
            Integer pageSize,
            Integer pageNumber
    ){}

    // 커서
    @Builder
    public record Cursor<T>(
            List<T> data,
            String nextCursor,
            Boolean hasNext,
            Integer pageSize
    ){}
}
