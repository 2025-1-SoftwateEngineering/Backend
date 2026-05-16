package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.enums.ClueType;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class CrosswordConverter {

    public static Crossword toCrossword(
            Long solvedCoin
    ){
        return Crossword.builder()
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .solvedCoin(solvedCoin)
                .build();
    }

    public static CrosswordHint toCrosswordHint(
            Crossword crossword,
            String clueContent,
            ClueType clueType,
            String wordStartPoint,
            Word word
    ){
        return CrosswordHint.builder()
                .crossword(crossword)
                .clueContent(clueContent)
                .clueType(clueType)
                .word(word)
                .wordStartPoint(wordStartPoint)
                .build();
    }
}
