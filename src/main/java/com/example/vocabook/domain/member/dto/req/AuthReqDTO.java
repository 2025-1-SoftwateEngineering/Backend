package com.example.vocabook.domain.member.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthReqDTO {

    // 회원가입
    public record SignUp(
            @NotBlank(message = "이름은 빈칸일 수 없습니다.")
            String nickname,
            @NotBlank(message = "이메일은 빈칸일 수 없습니다.")
            @Email(message = "이메일 형식을 입력해주세요.")
            String email,
            @NotBlank(message = "비밀번호는 빈칸일 수 없습니다.")
            String password
    ){}

    // 로그인
    public record Login(
            @NotBlank(message = "이메일은 빈칸일 수 없습니다.")
            @Email(message = "이메일 형식을 입력해주세요.")
            String email,
            @NotBlank(message = "비밀번호는 빈칸일 수 없습니다.")
            String password
    ){}

    // 토큰 재발급
    public record Reissue(
            @NotBlank(message = "리프레시 토큰은 빌칸일 수 없습니다.")
            String refreshToken
    ){}
}
