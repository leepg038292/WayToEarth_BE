package com.waytoearth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/v1/auth/**").permitAll()  // 인증 API는 모두 허용
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger 허용
//                        .requestMatchers("/actuator/health").permitAll()  // 헬스체크 허용
//                        .anyRequest().authenticated()
                          .anyRequest().permitAll()
                );

        return http.build();
    }
}