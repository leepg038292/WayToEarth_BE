package com.waytoearth.config.backendtest;

import com.waytoearth.entity.User.User;
import com.waytoearth.entity.enums.AgeGroup;
import com.waytoearth.entity.enums.Gender;
import com.waytoearth.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("postman")
public class PostmanTestDataConfig {

    @Bean
    CommandLineRunner initTestUser(UserRepository userRepository) {
        return args -> {
            //  MockAuthFilter에서 사용하는 userId=1에 맞춰 사용자 생성
            if (!userRepository.existsById(1L)) {
                userRepository.save(
                        User.builder()
                                .kakaoId(1234L)
                                .nickname("postman_user")
                                .residence("Seoul")
                                .ageGroup(AgeGroup.TWENTIES)
                                .gender(Gender.MALE)
                                .weeklyGoalDistance(new BigDecimal("10.0"))
                                .isOnboardingCompleted(true)
                                .build()
                );
                System.out.println(" Mock 사용자 생성 완료: userId=1");
            }

            //  추가 테스트 사용자들도 생성
            for (long i = 2; i <= 5; i++) {
                if (!userRepository.existsById(i)) {
                    userRepository.save(
                            User.builder()
                                    .kakaoId(1000L + i)
                                    .nickname("test_user_" + i)
                                    .residence("Seoul")
                                    .ageGroup(AgeGroup.TWENTIES)
                                    .gender(Gender.MALE)
                                    .weeklyGoalDistance(new BigDecimal("5.0"))
                                    .isOnboardingCompleted(true)
                                    .build()
                    );
                }
            }

            System.out.println(" 모든 테스트 사용자 준비 완료");
        };
    }
}