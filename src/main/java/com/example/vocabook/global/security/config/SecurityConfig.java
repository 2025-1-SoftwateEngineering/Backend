package com.example.vocabook.global.security.config;

import com.example.vocabook.global.security.exception.CustomAccessDeniedHandler;
import com.example.vocabook.global.security.exception.CustomEntryPoint;
import com.example.vocabook.global.security.filter.JwtAuthFilter;
import com.example.vocabook.global.security.service.CustomUserDetailsService;
import com.example.vocabook.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    // Public API
    @Value("${public.url}")
    private String[] allowUris;

    // Admin API
    private final String[] adminUris = {
            "/admin/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 요청 URI 설정
                .authorizeHttpRequests(requests -> requests
                        // 허용 URI
                        .requestMatchers(allowUris).permitAll()
                        // 어드민용 URI
                        .requestMatchers(adminUris).hasAuthority("ROLE_ADMIN")
                        // 그 이외 URI는 인증 필요 (Private API)
                        .anyRequest().authenticated()
                )
                // 기본제공 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 세선 셜정
                .sessionManagement(session -> session
                        // 세션 사용 X
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // JWT 토큰 인증 필터 활성화
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                // 인증되지 않았거나 거부된 경우
                .exceptionHandling(exception -> exception
                        // 인가 실패 (403)
                        .accessDeniedHandler(customAccessDeniedHandler())
                        // 인증 실패 (401)
                        .authenticationEntryPoint(customEntryPoint())
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, pathMatcher(), customUserDetailsService);
    }

    @Bean
    public PathMatcher pathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public CustomEntryPoint customEntryPoint() {
        return new CustomEntryPoint();
    }
}