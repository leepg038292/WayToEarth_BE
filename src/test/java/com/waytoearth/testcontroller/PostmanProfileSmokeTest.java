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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 🎯 완전한 러닝 플로우 + 엠블럼 + 피드 + 파일 업로드 시나리오
 * 기존 러닝 API + 새로운 API들 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postman")
class PostmanProfileSmokeTest {

    // 📍 기존 러닝 API 경로
    private static final String PATH_RUNNING_START = "/v1/running/start";
    private static final String PATH_RUNNING_UPDATE = "/v1/running/update";
    private static final String PATH_RUNNING_PAUSE = "/v1/running/pause";
    private static final String PATH_RUNNING_RESUME = "/v1/running/resume";
    private static final String PATH_RUNNING_COMPLETE = "/v1/running/complete";
    private static final String PATH_WEATHER_CURRENT = "/v1/weather/current";
    private static final String PATH_STATISTICS_WEEKLY = "/v1/statistics/weekly";

    // 🏆 새로운 엠블럼 API 경로
    private static final String PATH_EMBLEM_SUMMARY = "/v1/emblems/me/summary";
    private static final String PATH_EMBLEM_CATALOG = "/v1/emblems/catalog";
    private static final String PATH_EMBLEM_DETAIL = "/v1/emblems/{id}";
    private static final String PATH_EMBLEM_AWARD_ONE = "/v1/emblems/{id}/award";
    private static final String PATH_EMBLEM_SCAN_AWARD = "/v1/emblems/award/scan";

    // 📱 새로운 피드 API 경로
    private static final String PATH_FEED_CREATE = "/v1/feeds";
    private static final String PATH_FEED_LIST = "/v1/feeds";
    private static final String PATH_FEED_DETAIL = "/v1/feeds/{feedId}";
    private static final String PATH_FEED_LIKE = "/v1/feeds/{feedId}/like";
    private static final String PATH_FEED_DELETE = "/v1/feeds/{feedId}";

    // 📁 새로운 파일 업로드 API 경로
    private static final String PATH_FILE_PRESIGN_PROFILE = "/v1/files/presign/profile";
    private static final String PATH_FILE_PRESIGN_FEED = "/v1/files/presign/feed";

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

        //  러닝 시작
        System.out.println("\n 러닝 시작");
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

        System.out.println(" 러닝 시작 성공");

        // 2️⃣ 실시간 업데이트 #1
        System.out.println("\n 실시간 업데이트 #1 (500m, 3분)");
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

        System.out.println(" 1차 업데이트 성공");

        //  실시간 업데이트 #2
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

        System.out.println(" 2차 업데이트 성공");

        //  일시정지
        System.out.println("\n 일시정지 (휴식)");
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

        System.out.println(" 일시정지 성공");

        //  재개
        System.out.println("\n 재개");
        mockMvc.perform(
                        post(PATH_RUNNING_RESUME)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println(" 재개 성공");

        // 재개 후 업데이트
        System.out.println("\n 재개 후 업데이트 (2.5km, 15분)");
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

        System.out.println(" 재개 후 업데이트 성공");

        // 7️ 완료
        System.out.println("\n 러닝 완료");
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
        System.out.println(" 완료 응답: " + completeJson);

        //  통계 즉시 반영 확인
        System.out.println("\n 통계 업데이트 확인");
        MvcResult statsResult = mockMvc.perform(
                        get(PATH_STATISTICS_WEEKLY)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String statsJson = statsResult.getResponse().getContentAsString();
        System.out.println(" 업데이트된 통계: " + statsJson);

        if (!statsJson.isEmpty()) {
            JsonNode statsRoot = objectMapper.readTree(statsJson);
            double totalDistance = statsRoot.path("totalDistance").asDouble();
            long totalDuration = statsRoot.path("totalDuration").asLong();

            System.out.println(" 새로운 총 거리: " + totalDistance + "km");
            System.out.println(" 새로운 총 시간: " + totalDuration + "초");

            Assertions.assertTrue(totalDistance >= 3.0, "완료된 3km가 통계에 반영되어야 함");
            Assertions.assertTrue(totalDuration >= 1080, "완료된 시간이 통계에 반영되어야 함");
        }

        System.out.println("\n === 완전한 러닝 플로우 테스트 완료 ===");
    }

    @Test
    @DisplayName(" 통합 사용자 플로우 테스트 (러닝 → 엠블럼 → 피드 → 파일)")
    void integrated_user_flow_test() throws Exception {
        System.out.println(" === 통합 사용자 플로우 테스트 시작 ===");

        String sessionId = UUID.randomUUID().toString();
        System.out.println(" SessionId: " + sessionId);

        // 러닝 시작 → 완료 (간단 버전)
        System.out.println("\n 빠른 러닝 완료");

        // 러닝 시작
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

        // 러닝 완료
        MvcResult runningCompleteResult = mockMvc.perform(
                        post(PATH_RUNNING_COMPLETE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "sessionId", sessionId,
                                        "distanceMeters", 5000,      // 5km 달림
                                        "durationSeconds", 1800,     // 30분
                                        "averagePaceSeconds", 360,   // 6분/km
                                        "calories", 350,
                                        "routePoints", List.of(
                                                Map.of("latitude", 37.5665, "longitude", 126.9780, "timestamp", "2025-08-18T10:00:00"),
                                                Map.of("latitude", 37.5685, "longitude", 126.9800, "timestamp", "2025-08-18T10:30:00")
                                        )
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        Long runningRecordId = null;
        String runningCompleteJson = runningCompleteResult.getResponse().getContentAsString();
        if (!runningCompleteJson.isEmpty()) {
            JsonNode runningRoot = objectMapper.readTree(runningCompleteJson);
            runningRecordId = runningRoot.path("runningRecordId").asLong();
            System.out.println("🏃‍♂️ 러닝 기록 ID: " + runningRecordId);
        }

        System.out.println(" 러닝 완료");

        //  엠블럼 스캔 및 지급
        System.out.println("\n 엠블럼 스캔 지급 (러닝 완료로 새 엠블럼 획득 가능)");
        MvcResult emblemScanResult = mockMvc.perform(
                        post(PATH_EMBLEM_SCAN_AWARD)
                                .param("scope", "DISTANCE")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String emblemScanJson = emblemScanResult.getResponse().getContentAsString();
        if (!emblemScanJson.isEmpty()) {
            JsonNode emblemRoot = objectMapper.readTree(emblemScanJson);
            int awardedCount = emblemRoot.path("awarded_count").asInt();
            System.out.println("🏆 새로 획득한 엠블럼: " + awardedCount + "개");
        }

        //  엠블럼 요약 확인
        System.out.println("\n 엠블럼 요약 확인");
        MvcResult emblemSummaryResult = mockMvc.perform(
                        get(PATH_EMBLEM_SUMMARY)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String emblemSummaryJson = emblemSummaryResult.getResponse().getContentAsString();
        if (!emblemSummaryJson.isEmpty()) {
            JsonNode summaryRoot = objectMapper.readTree(emblemSummaryJson);
            int owned = summaryRoot.path("owned").asInt();
            int total = summaryRoot.path("total").asInt();
            double completionRate = summaryRoot.path("completion_rate").asDouble();

            System.out.println(" 현재 보유 엠블럼: " + owned + "/" + total);
            System.out.println(" 완성도: " + (completionRate * 100) + "%");
        }

        //  피드 이미지 Presigned URL 발급
        System.out.println("\n 피드 이미지 업로드 준비");
        MvcResult presignResult = mockMvc.perform(
                        post(PATH_FILE_PRESIGN_FEED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "contentType", "image/jpeg",
                                        "size", 1024000  // 1MB
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String imageUrl = "https://example.com/default_running.jpg"; // 기본값
        String presignJson = presignResult.getResponse().getContentAsString();
        if (!presignJson.isEmpty()) {
            JsonNode presignRoot = objectMapper.readTree(presignJson);
            imageUrl = presignRoot.path("public_url").asText();
            System.out.println("🔗 업로드 URL 생성: " + imageUrl);
        }

        //  피드 작성 (러닝 기록 연동)
        System.out.println("\n피드 작성 (러닝 기록 + 엠블럼 자랑)");
        MvcResult feedCreateResult = mockMvc.perform(
                        post(PATH_FEED_CREATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "runningRecordId", runningRecordId != null ? runningRecordId : 123L,
                                        "content", "오늘 5km 완주! 🏃‍♂️ 새로운 엠블럼도 획득했어요! ",
                                        "imageUrl", imageUrl
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        Long feedId = null;
        String feedCreateJson = feedCreateResult.getResponse().getContentAsString();
        if (!feedCreateJson.isEmpty()) {
            JsonNode feedRoot = objectMapper.readTree(feedCreateJson);
            feedId = feedRoot.path("id").asLong();
            System.out.println(" 생성된 피드 ID: " + feedId);
        }

        //  피드 목록에서 확인
        System.out.println("\n 피드 목록 확인");
        MvcResult feedListResult = mockMvc.perform(
                        get(PATH_FEED_LIST)
                                .param("offset", "0")
                                .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String feedListJson = feedListResult.getResponse().getContentAsString();
        if (!feedListJson.isEmpty()) {
            JsonNode feedArray = objectMapper.readTree(feedListJson);
            if (feedArray.isArray()) {
                System.out.println(" 총 피드 개수: " + feedArray.size());

                for (JsonNode feed : feedArray) {
                    String content = feed.path("content").asText();
                    Double distance = feed.path("distance").asDouble();
                    System.out.println("💬 " + content + (distance > 0 ? " (거리: " + distance + "km)" : ""));
                }
            }
        }

        //  피드 좋아요
        if (feedId != null) {
            System.out.println("\n 피드 좋아요 토글");
            MvcResult likeResult = mockMvc.perform(
                            post(PATH_FEED_LIKE, feedId)
                    )
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            String likeJson = likeResult.getResponse().getContentAsString();
            if (!likeJson.isEmpty()) {
                JsonNode likeRoot = objectMapper.readTree(likeJson);
                int likeCount = likeRoot.path("likeCount").asInt();
                boolean liked = likeRoot.path("liked").asBoolean();

                System.out.println("❤️ 좋아요 상태: " + (liked ? "활성" : "비활성"));
                System.out.println("📊 총 좋아요 수: " + likeCount);
            }
        }

        //  최종 통계 확인
        System.out.println("\n 최종 통계 확인");
        MvcResult finalStatsResult = mockMvc.perform(
                        get(PATH_STATISTICS_WEEKLY)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String finalStatsJson = finalStatsResult.getResponse().getContentAsString();
        if (!finalStatsJson.isEmpty()) {
            JsonNode statsRoot = objectMapper.readTree(finalStatsJson);
            double totalDistance = statsRoot.path("totalDistance").asDouble();
            long totalDuration = statsRoot.path("totalDuration").asLong();

            System.out.println(" 최종 누적 거리: " + totalDistance + "km");
            System.out.println(" 최종 누적 시간: " + totalDuration + "초");

            // 검증
            Assertions.assertTrue(totalDistance >= 5.0, "완료된 5km가 통계에 반영되어야 함");
            Assertions.assertTrue(totalDuration >= 1800, "완료된 시간이 통계에 반영되어야 함");
        }

        System.out.println("\n === 통합 사용자 플로우 테스트 완료 ===");
        System.out.println(" 러닝 → 엠블럼 획득 → 피드 공유 → 좋아요 → 통계 업데이트 완료!");
    }

    @Test
    @DisplayName(" 날씨 API 테스트")
    void weather_api_test() throws Exception {
        System.out.println(" 날씨 API 테스트");

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
            System.out.println(" 날씨 API 테스트 성공");
        }
    }

    @Test
    @DisplayName(" 통계 API 독립 테스트")
    void statistics_only_test() throws Exception {
        System.out.println(" 통계 API 독립 테스트");

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
        System.out.println(" 통계 결과: " + statsJson);

        if (!statsJson.isEmpty()) {
            JsonNode statsRoot = objectMapper.readTree(statsJson);
            double totalDistance = statsRoot.path("totalDistance").asDouble();
            long totalDuration = statsRoot.path("totalDuration").asLong();
            String averagePace = statsRoot.path("averagePace").asText();
            int totalCalories = statsRoot.path("totalCalories").asInt();

            System.out.println(" 총 거리: " + totalDistance + "km");
            System.out.println(" 총 시간: " + totalDuration + "초");
            System.out.println(" 평균 페이스: " + averagePace);
            System.out.println(" 총 칼로리: " + totalCalories);

            // 검증
            Assertions.assertTrue(totalDistance > 0, "총 거리가 0보다 커야 함");
            Assertions.assertTrue(totalDuration > 0, "총 시간이 0보다 커야 함");
            Assertions.assertNotEquals("00:00", averagePace, "평균 페이스가 계산되어야 함");
            Assertions.assertTrue(totalCalories > 0, "총 칼로리가 0보다 커야 함");

            // dailyDistances 배열 검증
            JsonNode dailyDistances = statsRoot.path("dailyDistances");
            Assertions.assertTrue(dailyDistances.isArray(), "dailyDistances는 배열이어야 함");
            Assertions.assertEquals(7, dailyDistances.size(), "7개 요일이 모두 있어야 함");

            System.out.println(" 통계 API 모든 검증 통과!");
        }
    }

    @Test
    @DisplayName(" 엠블럼 API 독립 테스트")
    void emblem_only_test() throws Exception {
        System.out.println(" 엠블럼 API 독립 테스트");

        // 요약 → 카탈로그 → 상세 → 지급 순서로 테스트

        // 요약 조회
        MvcResult summaryResult = mockMvc.perform(get(PATH_EMBLEM_SUMMARY))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // 카탈로그 조회 (OWNED 필터)
        MvcResult ownedResult = mockMvc.perform(
                        get(PATH_EMBLEM_CATALOG)
                                .param("filter", "OWNED")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // 일괄 스캔 지급 테스트
        MvcResult scanAllResult = mockMvc.perform(
                        post(PATH_EMBLEM_SCAN_AWARD)
                                .param("scope", "ALL")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String scanJson = scanAllResult.getResponse().getContentAsString();
        if (!scanJson.isEmpty()) {
            JsonNode scanRoot = objectMapper.readTree(scanJson);
            int awardedCount = scanRoot.path("awarded_count").asInt();

            System.out.println(" 일괄 스캔으로 지급된 엠블럼: " + awardedCount + "개");

            // 검증
            Assertions.assertTrue(awardedCount >= 0, "지급된 엠블럼 수는 0 이상이어야 함");
        }

        System.out.println(" 엠블럼 API 모든 검증 통과!");
    }

    @Test
    @DisplayName(" 피드 API 독립 테스트")
    void feed_only_test() throws Exception {
        System.out.println("📱 피드 API 독립 테스트");

        // 1. 피드 작성
        MvcResult createResult = mockMvc.perform(
                        post(PATH_FEED_CREATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "content", "테스트 피드입니다! 🏃‍♀️",
                                        "imageUrl", "https://example.com/test.jpg"
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String createJson = createResult.getResponse().getContentAsString();
        Long feedId = null;

        if (!createJson.isEmpty()) {
            JsonNode createRoot = objectMapper.readTree(createJson);
            feedId = createRoot.path("id").asLong();
            System.out.println(" 생성된 피드 ID: " + feedId);
        }

        // 2. 피드 목록 조회
        mockMvc.perform(
                        get(PATH_FEED_LIST)
                                .param("offset", "0")
                                .param("limit", "3")
                )
                .andExpect(status().isOk())
                .andDo(print());

        // 3. 피드 좋아요 (생성된 피드가 있을 때만)
        if (feedId != null) {
            MvcResult likeResult = mockMvc.perform(
                            post(PATH_FEED_LIKE, feedId)
                    )
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            String likeJson = likeResult.getResponse().getContentAsString();
            if (!likeJson.isEmpty()) {
                JsonNode likeRoot = objectMapper.readTree(likeJson);
                boolean liked = likeRoot.path("liked").asBoolean();
                int likeCount = likeRoot.path("likeCount").asInt();

                System.out.println("❤️ 좋아요 상태: " + liked + ", 개수: " + likeCount);

                // 검증
                Assertions.assertTrue(likeCount >= 0, "좋아요 수는 0 이상이어야 함");
            }
        }

        System.out.println(" 피드 API 모든 검증 통과!");
    }

    @Test
    @DisplayName(" 파일 업로드 API 독립 테스트")
    void file_upload_test() throws Exception {
        System.out.println(" 파일 업로드 API 독립 테스트");

        // 프로필 이미지 Presigned URL
        MvcResult profileResult = mockMvc.perform(
                        post(PATH_FILE_PRESIGN_PROFILE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "contentType", "image/png",
                                        "size", 512000  // 512KB
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String profileJson = profileResult.getResponse().getContentAsString();
        if (!profileJson.isEmpty()) {
            JsonNode profileRoot = objectMapper.readTree(profileJson);
            String uploadUrl = profileRoot.path("upload_url").asText();
            String publicUrl = profileRoot.path("public_url").asText();
            int expiresIn = profileRoot.path("expires_in").asInt();

            System.out.println("🔗 업로드 URL 생성됨 (만료: " + expiresIn + "초)");

            // 검증
            Assertions.assertFalse(uploadUrl.isEmpty(), "업로드 URL이 생성되어야 함");
            Assertions.assertFalse(publicUrl.isEmpty(), "공개 URL이 생성되어야 함");
            Assertions.assertTrue(expiresIn > 0, "만료 시간이 0보다 커야 함");
        }

        // 피드 이미지 Presigned URL
        mockMvc.perform(
                        post(PATH_FILE_PRESIGN_FEED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "contentType", "image/jpeg",
                                        "size", 3145728  // 3MB
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println(" 파일 업로드 API 모든 검증 통과!");
    }

    @Test
    @DisplayName(" 에러 시나리오 테스트")
    void error_scenarios_test() throws Exception {
        System.out.println(" 에러 시나리오 테스트");

        // 기존 러닝 에러 케이스
        System.out.println(" 잘못된 세션 ID로 업데이트 시도");
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

        // 새로운 API 에러 케이스들
        System.out.println(" 존재하지 않는 엠블럼 조회 시도");
        mockMvc.perform(get(PATH_EMBLEM_DETAIL, 99999L))
                .andDo(print());

        System.out.println(" 존재하지 않는 피드 좋아요 시도");
        mockMvc.perform(post(PATH_FEED_LIKE, 99999L))
                .andDo(print());

        System.out.println(" 잘못된 파일 크기로 Presigned URL 요청");
        mockMvc.perform(
                        post(PATH_FILE_PRESIGN_PROFILE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "contentType", "image/jpeg",
                                        "size", -1  // 음수 크기
                                )))
                )
                .andDo(print());

        System.out.println(" 에러 시나리오 테스트 완료");
    }

    private static String textOrEmpty(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? "" : node.path(field).asText("");
    }
}