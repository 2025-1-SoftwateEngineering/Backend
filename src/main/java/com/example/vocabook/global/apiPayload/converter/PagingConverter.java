package com.example.vocabook.global.apiPayload.converter;

import com.example.vocabook.global.apiPayload.dto.PagingResDTO;

import java.util.List;

public class PagingConverter {

    public static <T> PagingResDTO.Offset<T> toOffset(
            List<T> data,
            Integer pageNumber,
            Integer pageSize
    ){
        return PagingResDTO.Offset.<T>builder()
                .data(data)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }

    public static <T> PagingResDTO.Cursor<T> toCursor(
            List<T> data,
            String nextCursor,
            Boolean hasNext,
            Integer pageSize
    ){
        return PagingResDTO.Cursor.<T>builder()
                .data(data)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .pageSize(pageSize)
                .build();
    }
}
