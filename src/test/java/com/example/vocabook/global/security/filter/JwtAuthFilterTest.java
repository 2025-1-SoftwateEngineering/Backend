package com.example.vocabook.global.security.filter;

import com.example.vocabook.global.security.service.CustomUserDetailsService;
import com.example.vocabook.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.PathMatcher;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PathMatcher pathMatcher;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(jwtAuthFilter, "publicUrls", new String[]{"/api/public/**"});
    }

    @Test
    @DisplayName("Public URL은 필터링하지 않는다")
    void shouldNotFilter_PublicUrl() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/public/test");
        given(pathMatcher.match(anyString(), anyString())).willReturn(true);

        // when
        boolean result = ReflectionTestUtils.invokeMethod(jwtAuthFilter, "shouldNotFilter", request);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰이 없으면 필터를 그대로 통과한다 (인증되지 않음)")
    void doFilterInternal_NoToken() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("토큰이 유효하면 SecurityContext에 인증 정보가 저장된다")
    void doFilterInternal_ValidToken() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.isValid("valid_token")).willReturn(true);
        given(jwtUtil.isRefresh("valid_token")).willReturn(false);
        given(jwtUtil.getEmail("valid_token")).willReturn("test@example.com");

        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        given(customUserDetailsService.loadUserByUsername("test@example.com")).willReturn(userDetails);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 401 Unauthorized 에러 응답을 반환한다")
    void doFilterInternal_InvalidToken() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.isValid("invalid_token")).willThrow(new RuntimeException("Invalid token"));

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"isSuccess\":false");
        verify(filterChain, never()).doFilter(request, response);
    }
}
