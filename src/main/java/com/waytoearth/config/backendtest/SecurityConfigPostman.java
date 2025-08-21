package com.waytoearth.config.backendtest;

import com.waytoearth.security.mock.MockAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("postman")
@RequiredArgsConstructor
public class SecurityConfigPostman {

    private final MockAuthFilter mockAuthFilter;

    @Bean
    public SecurityFilterChain postmanFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/**", "/health", "/ping"
                        ).permitAll()
                        // 컨트롤러에서 @AuthUser를 쓰므로 인증 객체는 필요 -> authenticated()
                        .anyRequest().authenticated()
                )
                // JWT 필터는 여기서 절대 추가하지 않음
                .addFilterBefore(mockAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
