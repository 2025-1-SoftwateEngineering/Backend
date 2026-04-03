package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.AuthErrorCode;
import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.converter.AuthConverter;
import com.example.vocabook.domain.member.converter.MemberConverter;
import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.dto.res.AuthResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.exception.AuthException;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    // 회원가입
    @Transactional
    public AuthResDTO.SignUp signUp(
            AuthReqDTO.SignUp dto
    ) {
        // 이미 등록된 이메일인지 확인
        if (memberRepository.findByEmail(dto.email()).isPresent()) {
            throw new AuthException(AuthErrorCode.ALREADY_EXISTS);
        }

        // 엔티티 생성
        Member member = MemberConverter.toMember(dto, passwordEncoder.encode(dto.password()));

        // JWT 생성
        AuthMember authMember = new AuthMember(member);
        String accessToken = jwtUtil.createAccessToken(authMember);
        String refreshToken = jwtUtil.createRefreshToken(authMember);
        Date refreshExpiration = jwtUtil.getExpiration(refreshToken);

        // Refresh Token 업데이트
        member.updateRefreshToken(refreshToken);

        // 엔티티 저장
        memberRepository.save(member);

        // 응답 DTO 생성
        return AuthConverter.toSignUp(accessToken, refreshToken, refreshExpiration);
    }

    // 로그인
    @Transactional
    public AuthResDTO.Login login(
            AuthReqDTO.Login dto
    ) {

        // DB에서 사용자 찾기
        Member member = memberRepository.findByEmail(dto.email())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.WRONG_PASSWORD);
        }

        // JWT 토큰 생성
        AuthMember authMember = new AuthMember(member);
        String accessToken = jwtUtil.createAccessToken(authMember);
        String refreshToken = jwtUtil.createRefreshToken(authMember);
        Date refreshExpiration = jwtUtil.getExpiration(refreshToken);

        // 리프레시 토큰 업데이트
        member.updateRefreshToken(refreshToken);

        return AuthConverter.toLogin(accessToken, refreshToken, refreshExpiration);
    }

    // 토큰 재발급
    @Transactional
    public AuthResDTO.Reissue reissue(
            AuthReqDTO.Reissue dto
    ) {
        // 리프레시 토큰이 맞는지 검증
        if (!jwtUtil.isRefresh(dto.refreshToken())) {
            throw new AuthException(AuthErrorCode.NOT_REFRESH_TOKEN);
        }

        // 토큰에서 이메일 추출 -> 사용자 꺼내오기
        Member member = memberRepository.findByEmail(jwtUtil.getEmail(dto.refreshToken()))
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 사용자의 리프레시 토큰이 맞는지 검증
        if (!member.getRefreshToken().equals(dto.refreshToken())){
            throw new AuthException(AuthErrorCode.INVAILD_REFRESH_TOKEN);
        }

        // JWT 토큰 생성
        AuthMember authMember = new AuthMember(member);
        String accessToken = jwtUtil.createAccessToken(authMember);
        String refreshToken = jwtUtil.createRefreshToken(authMember);
        Date refreshExpiration = jwtUtil.getExpiration(refreshToken);

        // 리프레시 토큰 업데이트
        member.updateRefreshToken(refreshToken);

        return AuthConverter.toReissue(accessToken, refreshToken, refreshExpiration);
    }
}