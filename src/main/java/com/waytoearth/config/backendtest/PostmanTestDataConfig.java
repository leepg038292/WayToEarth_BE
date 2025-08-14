package com.waytoearth.config.backendtest;

import com.waytoearth.entity.User;
import com.waytoearth.entity.enums.AgeGroup;
import com.waytoearth.entity.enums.Gender;
import com.waytoearth.repository.UserRepository;
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
        return args -> userRepository.findByKakaoId(1234L)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(1234L)
                                .nickname("postman")
                                .residence("Seoul")
                                .ageGroup(AgeGroup.TWENTIES)
                                .gender(Gender.MALE)
                                .weeklyGoalDistance(new BigDecimal(10.0))
                                .build()
                ));
    }
}
