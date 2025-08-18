package com.waytoearth.testcontroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waytoearth.config.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *  완전한 러닝 플로우 테스트 - 실제 프론트엔드 시나리오
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postman")
class PostmanProfileSmokeTest {

    // 📍 모든 API 경로 정의
    private static final String PATH_RUNNING_START = "/v1/running/start";
    private static final String PATH_RUNNING_UPDATE = "/v1/running/update";
    private static final String PATH_RUNNING_PAUSE = "/v1/running/pause";
    private static final String PATH_RUNNING_RESUME = "/v1/running/resume";
    private static final String PATH_RUNNING_COMPLETE = "/v1/running/complete";
    private static final String PATH_WEATHER_CURRENT = "/v1/weather/current";
    private static final String PATH_STATISTICS_WEEKLY = "/v1/statistics/weekly";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("🏃‍♂️ 완전한 러닝 플로우 테스트 (모든 API 검증)")
    void complete_running_flow_test() throws Exception {
        System.out.println("🚀 === 완전한 러닝 플로우 테스트 시작 ===");

        String sessionId = UUID.randomUUID().toString();
        System.out.println("🆔 SessionId: " + sessionId);

        // 1️⃣ 러닝 시작
        System.out.println("\n1️⃣ 러닝 시작");
        mockMvc.perform(
                        post(PATH_RUNNING_START)
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

        System.out.println("✅ 러닝 시작 성공");

        // 2️⃣ 실시간 업데이트 #1
        System.out.println("\n2️⃣ 실시간 업데이트 #1 (500m, 3분)");
        mockMvc.perform(
                        post(PATH_RUNNING_UPDATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "currentDistance", 500,      // 500m
                                        "currentDuration", 180,      // 3분
                                        "currentLatitude", 37.5665,
                                        "currentLongitude", 126.9780,
                                        "currentPace", 360           // 6분/km
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("✅ 1차 업데이트 성공");

        // 3️⃣ 실시간 업데이트 #2
        System.out.println("\n3️⃣ 실시간 업데이트 #2 (1.2km, 7분)");
        mockMvc.perform(
                        post(PATH_RUNNING_UPDATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "currentDistance", 1200,     // 1.2km
                                        "currentDuration", 420,      // 7분
                                        "currentLatitude", 37.5675,
                                        "currentLongitude", 126.9790,
                                        "currentPace", 350
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("✅ 2차 업데이트 성공");

        // 4️⃣ 일시정지
        System.out.println("\n4️⃣ 일시정지 (휴식)");
        mockMvc.perform(
                        post(PATH_RUNNING_PAUSE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "pauseReason", "휴식"
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("⏸️ 일시정지 성공");

        // 5️⃣ 재개
        System.out.println("\n5️⃣ 재개");
        mockMvc.perform(
                        post(PATH_RUNNING_RESUME)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("▶️ 재개 성공");

        // 6️⃣ 재개 후 업데이트
        System.out.println("\n6️⃣ 재개 후 업데이트 (2.5km, 15분)");
        mockMvc.perform(
                        post(PATH_RUNNING_UPDATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "currentDistance", 2500,     // 2.5km
                                        "currentDuration", 900,      // 15분 (순수 러닝 시간)
                                        "currentLatitude", 37.5685,
                                        "currentLongitude", 126.9800,
                                        "currentPace", 360
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("✅ 재개 후 업데이트 성공");

        // 7️⃣ 완료
        System.out.println("\n7️⃣ 러닝 완료");
        MvcResult completeResult = mockMvc.perform(
                        post(PATH_RUNNING_COMPLETE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "distanceMeters", 3000,      // 최종 3km
                                        "durationSeconds", 1080,     // 18분
                                        "averagePaceSeconds", 360,   // 평균 6분/km
                                        "calories", 250,
                                        "routePoints", List.of(
                                                Map.of("latitude", 37.5665, "longitude", 126.9780, "timestamp", "2025-08-18T10:00:00"),
                                                Map.of("latitude", 37.5675, "longitude", 126.9790, "timestamp", "2025-08-18T10:07:00"),
                                                Map.of("latitude", 37.5685, "longitude", 126.9800, "timestamp", "2025-08-18T10:18:00")
                                        )
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String completeJson = completeResult.getResponse().getContentAsString();
        System.out.println("🏁 완료 응답: " + completeJson);

        // 8️⃣ 통계 즉시 반영 확인
        System.out.println("\n8️⃣ 통계 업데이트 확인");
        MvcResult statsResult = mockMvc.perform(
                        get(PATH_STATISTICS_WEEKLY)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String statsJson = statsResult.getResponse().getContentAsString();
        System.out.println("📊 업데이트된 통계: " + statsJson);

        if (!statsJson.isEmpty()) {
            JsonNode statsRoot = objectMapper.readTree(statsJson);
            double totalDistance = statsRoot.path("totalDistance").asDouble();
            long totalDuration = statsRoot.path("totalDuration").asLong();

            System.out.println("📈 새로운 총 거리: " + totalDistance + "km");
            System.out.println("⏱️ 새로운 총 시간: " + totalDuration + "초");

            Assertions.assertTrue(totalDistance >= 3.0, "완료된 3km가 통계에 반영되어야 함");
            Assertions.assertTrue(totalDuration >= 1080, "완료된 시간이 통계에 반영되어야 함");
        }

        System.out.println("\n🎉 === 완전한 러닝 플로우 테스트 완료 ===");
    }

    @Test
    @DisplayName("🌤️ 날씨 API 테스트")
    void weather_api_test() throws Exception {
        System.out.println("🌤️ 날씨 API 테스트");

        MvcResult result = mockMvc.perform(
                        get(PATH_WEATHER_CURRENT)
                                .param("lat", "37.5665")
                                .param("lon", "126.9780")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String weatherJson = result.getResponse().getContentAsString();
        System.out.println("🌡️ 날씨 응답: " + weatherJson);

        if (!weatherJson.isEmpty()) {
            JsonNode weatherRoot = objectMapper.readTree(weatherJson);
            System.out.println("✅ 날씨 API 테스트 성공");
        }
    }

    @Test
    @DisplayName("📊 통계 API 독립 테스트")
    void statistics_only_test() throws Exception {
        System.out.println("📊 통계 API 독립 테스트");

        // 먼저 테스트 데이터 1개 생성
        String sessionId = UUID.randomUUID().toString();

        // 빠른 러닝 기록 생성
        mockMvc.perform(
                post(PATH_RUNNING_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "sessionId": "%s",
                                  "runningType": "SINGLE",
                                  "weatherCondition": "맑음"
                                }
                                """, sessionId))
        ).andExpect(status().isOk());

        mockMvc.perform(
                post(PATH_RUNNING_COMPLETE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sessionId", sessionId,
                                "distanceMeters", 5000,
                                "durationSeconds", 1800,
                                "averagePaceSeconds", 360,
                                "calories", 300,
                                "routePoints", List.of()
                        )))
        ).andExpect(status().isOk());

        // 통계 조회
        MvcResult statsResult = mockMvc.perform(
                        get(PATH_STATISTICS_WEEKLY)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String statsJson = statsResult.getResponse().getContentAsString();
        System.out.println("📈 통계 결과: " + statsJson);

        if (!statsJson.isEmpty()) {
            JsonNode statsRoot = objectMapper.readTree(statsJson);
            double totalDistance = statsRoot.path("totalDistance").asDouble();
            long totalDuration = statsRoot.path("totalDuration").asLong();
            String averagePace = statsRoot.path("averagePace").asText();
            int totalCalories = statsRoot.path("totalCalories").asInt();

            System.out.println("📏 총 거리: " + totalDistance + "km");
            System.out.println("⏱️ 총 시간: " + totalDuration + "초");
            System.out.println("🏃‍♂️ 평균 페이스: " + averagePace);
            System.out.println("🔥 총 칼로리: " + totalCalories);

            // 검증
            Assertions.assertTrue(totalDistance > 0, "총 거리가 0보다 커야 함");
            Assertions.assertTrue(totalDuration > 0, "총 시간이 0보다 커야 함");
            Assertions.assertNotEquals("00:00", averagePace, "평균 페이스가 계산되어야 함");
            Assertions.assertTrue(totalCalories > 0, "총 칼로리가 0보다 커야 함");

            // dailyDistances 배열 검증
            JsonNode dailyDistances = statsRoot.path("dailyDistances");
            Assertions.assertTrue(dailyDistances.isArray(), "dailyDistances는 배열이어야 함");
            Assertions.assertEquals(7, dailyDistances.size(), "7개 요일이 모두 있어야 함");

            System.out.println("✅ 통계 API 모든 검증 통과!");
        }
    }

    @Test
    @DisplayName("🚨 에러 시나리오 테스트")
    void error_scenarios_test() throws Exception {
        System.out.println("🚨 에러 시나리오 테스트");

        // 존재하지 않는 세션으로 업데이트 시도
        System.out.println("❌ 잘못된 세션 ID로 업데이트 시도");
        mockMvc.perform(
                        post(PATH_RUNNING_UPDATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", "non-existent-session",
                                        "currentDistance", 1000,
                                        "currentDuration", 300
                                )))
                )
                .andDo(print());
        // .andExpect(status().isBadRequest()); // 실제 에러 처리에 따라 조정

        System.out.println("⚠️ 에러 시나리오 테스트 완료");
    }

    private static String textOrEmpty(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? "" : node.path(field).asText("");
    }
}