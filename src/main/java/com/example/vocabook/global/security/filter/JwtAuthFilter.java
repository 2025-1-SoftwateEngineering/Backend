package com.example.vocabook.global.security.filter;

import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.security.service.CustomUserDetailsService;
import com.example.vocabook.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PathMatcher pathMatcher;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${public.url}")
    private String[] publicUrls;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        // 만약 Public API로 온 요청이라면
        for (String url : publicUrls) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {

            // 토큰 가져오기
            String token = request.getHeader("Authorization");
            // token이 없거나 Bearer가 아니면 넘기기
            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Bearer이면 추출
            token = token.replace("Bearer ", "");

            // AccessToken 검증하기: Refresh Token이 아니면서 해독 가능하면 O
            if (jwtUtil.isValid(token) && !jwtUtil.isRefresh(token)) {
                // 토큰에서 이메일 추출
                String email = jwtUtil.getEmail(token);
                // 인증 객체 생성: 이메일로 찾아온 뒤, 인증 객체 생성
                UserDetails user = customUserDetailsService.loadUserByUsername(email);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                // 인증 완료 후 SecurityContextHolder에 넣기
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {

            ObjectMapper mapper = new ObjectMapper();
            BaseErrorCode code = GeneralErrorCode.UNAUTHORIZED;

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(code.getStatus().value());

            ApiResponse<Void> errorResponse = ApiResponse.onFailure(code,null);

            mapper.writeValue(response.getOutputStream(), errorResponse);
        }
    }
}
