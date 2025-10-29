package com.waytoearth.testcontroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waytoearth.config.jwt.JwtAuthenticationFilter;
import com.waytoearth.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 핵심 API 동작 확인용 테스트 (간소화 버전)
 * - 새로운 공통 응답 구조 적용
 * - 실제 동작만 확인, 상세 검증은 최소화
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("waytoearth-dev")
class PostmanProfileSmokeTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setupSecurity() {
        AuthenticatedUser fakeUser = new AuthenticatedUser(1L, com.waytoearth.entity.enums.UserRole.USER);
        var auth = new UsernamePasswordAuthenticationToken(fakeUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("러닝 기본 플로우 동작 확인")
    void running_basic_flow() throws Exception {
        System.out.println("=== 러닝 기본 플로우 테스트 ===");

        String sessionId = UUID.randomUUID().toString();

        // 러닝 시작
        mockMvc.perform(
                        post("/v1/running/start")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                {
                                  "sessionId": "%s",
                                  "runningType": "SINGLE",
                                  "weatherCondition": "맑음"
                                }
                                """, sessionId))
                )
                .andExpect(status().isOk())
                .andDo(print());

        // 러닝 완료
        mockMvc.perform(
                        post("/v1/running/complete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "distanceMeters", 1000,
                                        "durationSeconds", 300,
                                        "averagePaceSeconds", 300,
                                        "calories", 100,
                                        "routePoints", List.of()
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("러닝 플로우 동작 확인 완료 ");
    }

    @Test
    @DisplayName("주요 API들 응답 확인")
    void major_apis_response_check() throws Exception {
        System.out.println("=== 주요 API 응답 확인 테스트 ===");

        // 통계 API
        mockMvc.perform(get("/v1/statistics/weekly"))
                .andExpect(status().isOk())
                .andDo(print());

        // 엠블럼 요약 API
        mockMvc.perform(get("/v1/emblems/me/summary"))
                .andExpect(status().isOk())
                .andDo(print());

        // 날씨 API
        mockMvc.perform(
                        get("/v1/weather/current")
                                .param("lat", "37.5665")
                                .param("lon", "126.9780")
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("주요 API 응답 확인 완료 ");
    }

    @Test
    @DisplayName("피드 기본 동작 확인")
    void feed_basic_operations() throws Exception {
        System.out.println("=== 피드 기본 동작 테스트 ===");

        // 피드 생성
        MvcResult createResult = mockMvc.perform(
                        post("/v1/feeds")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "content", "테스트 피드",
                                        "imageUrl", "https://example.com/test.jpg"
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // 피드 목록 조회
        mockMvc.perform(
                        get("/v1/feeds")
                                .param("offset", "0")
                                .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("피드 기본 동작 확인 완료 ");
    }

    @Test
    @DisplayName("파일 업로드 API 동작 확인")
    void file_upload_basic_check() throws Exception {
        System.out.println("=== 파일 업로드 API 동작 테스트 ===");

        mockMvc.perform(
                        post("/v1/files/presign/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "contentType", "image/jpeg",
                                        "size", 1024000
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("파일 업로드 API 동작 확인 완료 ");
    }

    @Test
    @DisplayName("사용자 API 동작 확인")
    void user_api_basic_check() throws Exception {
        System.out.println("=== 사용자 API 동작 테스트 ===");

        // 내 정보 조회
        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isOk())
                .andDo(print());

        // 사용자 요약 조회
        mockMvc.perform(get("/v1/users/me/summary"))
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("사용자 API 동작 확인 완료 ");
    }
}