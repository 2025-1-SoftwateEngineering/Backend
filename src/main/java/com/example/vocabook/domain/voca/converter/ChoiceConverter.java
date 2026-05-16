package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class ChoiceConverter {

    public static Choice toChoice(
            Long solvedCoin
    ){
        return Choice.builder()
                .solvedCoin(solvedCoin)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

    public static ChoiceQuestion toChoiceQuestion(
            Choice choice,
            Word word,
            Boolean isWord
    ){
        return ChoiceQuestion.builder()
                .word(word)
                .isWord(isWord)
                .choice(choice)
                .build();
    }
}
