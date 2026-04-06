package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.entity.Member;

public class MemberConverter {

    // 회원가입 (Member)
    public static Member toMember(
            AuthReqDTO.SignUp dto,
            String saltedPassword
    ){
        return Member.builder()
                .email(dto.email())
                .password(saltedPassword)
                .nickname(dto.nickname())
                .build();
    }
}
