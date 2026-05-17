package com.example.vocabook.domain.voca.converter;

import com.example.vocabook.domain.member.dto.req.AdminReqDTO;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;

public class WordConverter {

    public static Word toWord(
            AdminReqDTO.AddWord dto
    ){
        return Word.builder()
                .englishWord(dto.english().toLowerCase())
                .meaning(dto.meaning())
                .voca(null)
                .build();
    }

    public static Word toWord(
            AdminReqDTO.AddWord dto,
            Voca voca
    ){
        return Word.builder()
                .englishWord(dto.english().toLowerCase())
                .meaning(dto.meaning())
                .voca(voca)
                .build();
    }
}
