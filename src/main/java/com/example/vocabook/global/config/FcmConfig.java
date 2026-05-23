package com.example.vocabook.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    @Value("${fcm.path}")
    private String path;

    private final ResourceLoader resourceLoader;

    @Bean
    public FirebaseApp firebaseApp() {
        // 이미 초기화된 경우 기존 인스턴스 반환 (테스트 시 여러 컨텍스트 로드 대비)
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try {
            Resource resource = resourceLoader.getResource(path);

            // 파일이 없는 경우 (CI/테스트 환경) → 빈 생성 건너뜀
            // 테스트에서는 @MockitoBean FirebaseMessaging으로 대체됨
            if (!resource.exists()) {
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(resource.getInputStream())
                    )
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        // firebaseApp이 null인 경우 (파일 없음) → null 반환
        // 테스트에서는 @MockitoBean FirebaseMessaging으로 대체됨
        if (firebaseApp == null) {
            return null;
        }
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
