package com.waytoearth.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.firebase.config-path:}")
    private String firebaseConfigPath;

    @Value("${fcm.firebase.config-json:}")
    private String firebaseConfigJson;

    @PostConstruct
    public void initialize() {
        try {
            GoogleCredentials credentials;

            // 1. JSON 환경변수 우선 (Docker 배포 시)
            if (firebaseConfigJson != null && !firebaseConfigJson.isBlank()) {
                log.info("🔧 Firebase 초기화: 환경변수 JSON 사용");
                credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(firebaseConfigJson.getBytes(StandardCharsets.UTF_8))
                );
            }
            // 2. 파일 경로 (로컬 개발 시)
            else if (firebaseConfigPath != null && !firebaseConfigPath.isBlank()) {
                log.info("🔧 Firebase 초기화: 파일 경로 사용 ({})", firebaseConfigPath);
                credentials = GoogleCredentials.fromStream(
                    new FileInputStream(firebaseConfigPath)
                );
            }
            // 3. 둘 다 없으면 에러
            else {
                throw new IllegalStateException("FCM 설정이 없습니다. fcm.firebase.config-json 또는 fcm.firebase.config-path를 설정하세요.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK 초기화 성공");
            }
        } catch (IOException e) {
            log.error("❌ Firebase Admin SDK 초기화 실패: {}", e.getMessage());
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
}
